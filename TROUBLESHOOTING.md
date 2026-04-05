# Docker Troubleshooting Guide

## Diagnosis Flowchart

```
Something's not working?
    ↓
START HERE: docker-compose ps
    ↓
─────────────────────────────────────────────
│  Are all containers RUNNING/HEALTHY?       │
│  (Status column shows "Up" or "Healthy")  │
─────────────────────────────────────────────
    ↓ YES                    ↓ NO
    │                        │
Check logs:                  │
docker-compose logs -f       ├→ docker-compose logs
    │                        │
    ├→ "port already         ├→ See "Container Won't Start"
    │    in use"             
    │    Fix: Restart system/kill process
    │
    ├→ "connection refused"  ├→ See "Connection Issues"
    │
    ├→ "no such host"        ├→ See "DNS Issues"
    │
    └→ Everything OK?
       Service still broken?
       ↓
       Check browser console
       Check network tab
       ↓
       See "Frontend Issues"
```

---

## Quick Diagnosis Commands

```bash
# 1. Check if containers are running
docker-compose ps

# 2. View all logs
docker-compose logs

# 3. View specific service logs (most recent)
docker-compose logs --tail 30 backend

# 4. Follow logs in real-time
docker-compose logs -f

# 5. Check if ports are open
netstat -tuln | grep -E ':(3000|8080|5432)'

# 6. Inspect network
docker network inspect cc_project_dams_network

# 7. Test DNS inside container
docker-compose exec frontend nslookup backend

# 8. Test connectivity from frontend to backend
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health
```

---

## Container Won't Start

### Symptom: Status shows "Exited" or keeps restarting

```bash
# Check logs
docker-compose logs backend

# Look for patterns:
# - "Address already in use" → Port conflict
# - "Connection refused" → Dependency not ready
# - "OutOfMemoryError" → Memory limit issue
# - "ERROR" → Application error
```

### Solution by Error Type

#### Port Already in Use

```
Error: bind: address already in use
```

**Find and kill the process:**
```bash
# Find process on port 8080
lsof -i :8080

# Kill it
kill -9 <PID>

# Or change port in docker-compose.yml
ports:
  - "8081:8080"  # Use different host port
```

**Then restart:**
```bash
docker-compose up -d backend
```

---

#### Build Failure

```
Error: RUN npm install failed
```

**Rebuild without cache:**
```bash
# Clean rebuild
docker-compose build --no-cache frontend

# If still failing, try:
docker system prune -af
docker-compose build --no-cache frontend
```

**Common causes:**
- Node/Java version mismatch
- Network issues during build
- Dependency version conflicts

---

#### Out of Memory

```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

**Increase memory:**
```yaml
# In docker-compose.yml
backend:
  environment:
    JAVA_TOOL_OPTIONS: "-Xmx512m -Xss512k"  # Increase from 300m
```

**Then restart:**
```bash
docker-compose up -d backend
```

---

## Connection Issues

### Frontend Can't Reach Backend

#### Symptom 1: Browser console shows network error

```
GET http://backend:8080/api/users 404 
or
GET http://backend:8080/api/users ERR_NAME_NOT_RESOLVED
```

**Step 1: Verify backend is running**
```bash
docker-compose ps backend

# Expected: Status "Up (healthy)"
```

**Step 2: Verify from inside frontend container**
```bash
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health

# Expected: JSON response starting with {"status":"UP"...}
# If: "command not found" → Try: curl http://backend:8080/actuator/health
# If: "Connection refused" → Backend not listening
```

**Step 3: Check Docker network**
```bash
docker network inspect cc_project_dams_network

# Verify both frontend and backend connected:
# "Containers": {
#   "dams_backend": {...},
#   "dams_frontend": {...}
# }
```

**Step 4: Check frontend's environment variable**
```bash
docker-compose exec frontend sh -c 'echo $VITE_BACK_END_URL'

# Expected: http://backend:8080
# If: nothing/wrong → Environment variable not passed
```

**Fix: Rebuild frontend with environment**
```bash
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

---

#### Symptom 2: Works on localhost, not from container

**Cause:** API URL hardcoded to `localhost` instead of service name

**Frontend code:**
```javascript
// WRONG (inside docker-compose):
const api = axios.create({
  baseURL: 'http://localhost:8080/api'  // localhost = frontend container
});

// CORRECT (inside docker-compose):
const api = axios.create({
  baseURL: `${process.env.VITE_BACK_END_URL}/api`  // backend = backend container
});
```

**Fix:**
```bash
# Update frontend code
# Then rebuild
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

---

### Backend Can't Reach Database

#### Symptom: Backend logs show connection refused

```
Unable to obtain a connection from the DriverManager
Connection refused to host: localhost
```

**Step 1: Verify database is running**
```bash
docker-compose ps postgres

# Expected: Status "Up (healthy)"
```

**Step 2: Test connection from backend container**
```bash
docker-compose exec backend nc -zv postgres 5432

# Expected: Connection to postgres 5432 port [tcp/*] succeeded!
# If: Connection refused → Postgres not ready or not connected to network
```

**Step 3: Check backend's database URL**
```bash
docker-compose exec backend bash
echo $SPRING_DATASOURCE_URL

# Expected: jdbc:postgresql://postgres:5432/mydb
# If: localhost → WRONG! Should be 'postgres'
# If: not set → Environment variable not passed
```

**Fix: Application.properties**
```properties
# Use environment variable interpolation
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres:5432/mydb}
                      ↑ Docker will inject this
```

**Rebuild backend:**
```bash
docker-compose build --no-cache backend
docker-compose up -d backend
```

---

#### Symptom: Connection timeout (not refused)

```
Unable to obtain a connection from the DriverManager
Timeout attempting to get a connection
```

**Cause:** Backend trying to connect before PostgreSQL is ready

**Already solved in our docker-compose.yml:**
```yaml
postgres:
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U myuser"]
    interval: 10s
    timeout: 5s
    retries: 5

backend:
  depends_on:
    postgres:
      condition: service_healthy  # Wait for health check
```

**If still happening:**
```bash
# Increase wait times
# Edit docker-compose.yml:
postgres:
  healthcheck:
    retries: 10  # From 5 to 10

# Restart
docker-compose down -v
docker-compose up --build
```

---

## DNS Issues

### "No such host" Error

```
Exception: No such host is known (hostname.resolution.failed)
```

**Step 1: Check DNS inside container**
```bash
docker-compose exec backend nslookup postgres

# Expected:
# Name:     postgres
# Address:  172.20.0.3

# If: nslookup: command not found
docker-compose exec backend ping postgres
```

**Step 2: Verify container connected to network**
```bash
docker network inspect cc_project_dams_network | grep -A 20 'Containers'

# Should list all containers with their IPs
```

**Step 3: Verify service name in docker-compose.yml**
```yaml
services:
  postgres:        # ← This is the service name (hostname)
    container_name: dams_postgres  # ← This is for Docker CLI
```

**Connection uses service name, not container_name:**
```properties
# CORRECT:
spring.datasource.url=jdbc:postgresql://postgres:5432/mydb
                                       ↑ service name

# WRONG:
spring.datasource.url=jdbc:postgresql://dams_postgres:5432/mydb
                                       ↑ container_name (won't resolve in bridge)
```

---

## Frontend Issues

### Blank Page, No Errors

#### Symptom: Browser shows blank, console empty

**Step 1: Check if Nginx is serving content**
```bash
docker-compose exec frontend nginx -T

# Expected: "configuration file ... test is successful"
# If: errors → Nginx config problem
```

**Step 2: Manually fetch the page**
```bash
docker-compose exec frontend wget -q -O- http://localhost:80/index.html | head -20

# Expected: HTML content with <head>, <script>, etc.
# If: empty/404 → dist folder not deployed
```

**Step 3: Check if build happened**
```bash
docker-compose exec frontend ls -la /usr/share/nginx/html/

# Expected: index.html, js folder, css folder
# If: empty → npm run build didn't execute
```

**Fix: Rebuild frontend**
```bash
docker-compose build --no-cache frontend
docker-compose up -d frontend

# Wait 10 seconds, then refresh browser
```

---

### "Cannot GET /api/..."

#### Symptom: Browser shows "Cannot GET" message (Nginx error page)

**Cause:** Nginx trying to serve API request as a file, not forwarding to backend

**Nginx config should proxy to backend:**
```nginx
# WRONG - Nginx config:
server {
  root /usr/share/nginx/html;
  location / {
    try_files $uri $uri/;  # Only serves files
  }
}

# CORRECT:
# API requests go to backend outside of Docker
# OR add proxy to docker-compose
```

**Frontend should call backend, not through Nginx:**
```javascript
// API calls go DIRECTLY to backend:port, not through Nginx
axios.create({
  baseURL: 'http://backend:8080/api'  // Direct to backend
})

// Nginx serves React SPA:
// http://localhost:3000 → Nginx → index.html → React app
```

---

### Console Shows CORS Errors

```
Access to XMLHttpRequest at 'http://backend:8080/api/users' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Backend needs CORS configuration:**
```java
// In Spring Boot configuration
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")  // Frontend URL
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowCredentials(true);
            }
        };
    }
}
```

**Restart backend:**
```bash
docker-compose build --no-cache backend
docker-compose up -d backend
```

---

## Network Debugging

### Inspect Network Details

```bash
# Show network config
docker network inspect cc_project_dams_network

# Expected output shows:
# - all connected containers
# - their IP addresses
# - gateway IP (usually .1)
```

### Check Connectivity Between Containers

```bash
# Test backend → database
docker-compose exec backend nc -zv postgres 5432

# Test frontend → backend
docker-compose exec frontend nc -zv backend 8080

# Test from any container to any other
docker-compose exec <service> nc -zv <other_service> <port>
```

### Monitor Network Traffic

```bash
# Install tcpdump (if not available)
# See traffic on docker0 interface
sudo tcpdump -i docker0 port 8080

# Or use docker debug
docker debug <container_id>
```

---

## Database Issues

### Database Empty After Restart

#### Symptom: All data gone after `docker-compose down`

```bash
# Did you use -v flag?
docker-compose down -v
          ↑ This removes volumes (database data)!
```

**Solution:**
```bash
# Don't use -v if you want to keep data
docker-compose down          # Keeps volumes

# Reset completely (delete data)
docker-compose down -v       # Removes volumes
```

**Check volume status:**
```bash
# List volumes
docker volume ls

# See where data is stored
docker volume inspect cc_project_postgres_data

# Manual backup of database
docker-compose exec postgres pg_dump -U myuser mydb > backup.sql
```

---

### Can't Connect to Database from Host

#### Symptom: psql from host machine can't connect

```bash
# This won't work directly
psql -U myuser -d mydb -h localhost

# Instead, use docker-compose
docker-compose exec postgres psql -U myuser -d mydb

# Or connect from backend container
docker-compose exec backend bash
# Then use SQL tools from inside
```

---

### Database Migrations Not Running

#### Symptom: Tables don't exist, Hibernate DDL not working

```
Could not retrieve transactionIsolationLevel
error: db error: FATAL: database "mydb" does not exist
```

**Verify HIBERNATE_DDL setting:**
```bash
docker-compose exec backend bash
echo $SPRING_JPA_HIBERNATE_DDL_AUTO

# Expected: create, update, create-drop, or validate
# If: not set → Check .env file and docker-compose.yml
```

**Restart with proper DDL:**
```yaml
# In docker-compose.yml
backend:
  environment:
    SPRING_JPA_HIBERNATE_DDL_AUTO: create  # Or: update
```

**Restart database and backend:**
```bash
docker-compose down -v
docker-compose up --build
```

---

## Performance Issues

### High CPU/Memory Usage

#### Check resource limits

```bash
# Monitor in real-time
docker stats

# Check limits in docker-compose.yml
backend:
  deploy:
    resources:
      limits:
        cpus: '1'
        memory: 512M
```

**If backend using too much memory:**
```yaml
backend:
  environment:
    JAVA_TOOL_OPTIONS: "-Xmx256m -Xss512k"  # Reduce heap size
```

---

### Slow Startup Time

```
Expected:
- PostgreSQL: 2-3 seconds
- Backend: 30-40 seconds
- Frontend: 5-10 seconds
Total: ~45-55 seconds first time
Rebuilds: 10-20 seconds (with cache)
```

**Speed up rebuilds:**
```bash
# Use --build (uses cache)
docker-compose up --build     # FAST (uses cache)

# Use --no-cache (slower)
docker-compose build --no-cache  # SLOW (rebuilds everything)
```

---

## Final Nuclear Option

If nothing works:

```bash
# Stop everything
docker-compose down -v

# Remove all unused docker resources
docker system prune -af

# Clean slate
rm -rf docker-compose.override.yml
docker container prune -f
docker volume prune -f

# Rebuild completely
docker-compose build --no-cache
docker-compose up

# Monitor
docker-compose logs -f
```

---

## Getting Help

When asking for help, provide:

```bash
# 1. Status
docker-compose ps

# 2. Full error logs
docker-compose logs

# 3. Specific service logs
docker-compose logs backend

# 4. Network connectivity
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health

# 5. Docker version
docker --version
docker-compose --version
```

---

**Remember:** The logs are your friend! Always check `docker-compose logs -f` first.
