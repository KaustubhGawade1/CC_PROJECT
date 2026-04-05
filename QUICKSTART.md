# Quick Start Guide - DAMS Docker

## Prerequisites Check

```bash
# Verify Docker is installed
docker --version
docker-compose --version

# Verify required ports are free
netstat -tuln | grep -E ':(3000|8080|5432)'
# If nothing shows up, ports are free!
```

---

## 30-Second Startup

```bash
# Navigate to project root
cd /home/kaustubh/projects/CC_Project

# Build and run everything
docker-compose up --build

# Wait for output like:
# ✓ Container dams_postgres  Healthy
# ✓ Container dams_backend   Started
# ✓ Container dams_frontend  Started
```

**Then open your browser:**

| What | URL |
|------|-----|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| Health Check | http://localhost:8080/actuator/health |

---

## Using the Helper Script

```bash
# Make it executable
chmod +x docker-scripts.sh

# Start everything
./docker-scripts.sh start

# Start in background (recommended for development)
./docker-scripts.sh start-bg

# View logs
./docker-scripts.sh logs

# Stop services
./docker-scripts.sh stop

# Full list of commands
./docker-scripts.sh help
```

---

## What Each Service Does

### PostgreSQL (Database)
- **Port:** 5432
- **Container:** `dams_postgres`
- **Credentials:** myuser / mypassword
- **Database:** mydb
- **Status:** Checks every 10 seconds if ready
- **Data:** Persisted in `postgres_data` volume

### Backend (Spring Boot API)
- **Port:** 8080
- **Container:** `dams_backend`
- **Connects to:** PostgreSQL via `jdbc:postgresql://postgres:5432/mydb`
- **Health:** Checks `/actuator/health` every 30 seconds
- **Waits for:** PostgreSQL to be healthy before starting
- **Memory:** Limited to 300MB (configurable via JAVA_TOOL_OPTIONS)

### Frontend (React + Nginx)
- **Port:** 3000
- **Container:** `dams_frontend`
- **API Base:** Calls `http://backend:8080/api`
- **Waits for:** Backend service to start
- **Nginx:** Routes all requests through SPA rules

---

## Common Tasks

### 1. Rebuild After Code Changes

```bash
# Rebuild backend only
docker-compose build backend --no-cache
docker-compose up -d backend

# Or restart frontend
docker-compose build frontend --no-cache
docker-compose up -d frontend

# Or everything
docker-compose up --build
```

### 2. View Application Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Last 50 lines
docker-compose logs --tail 50
```

### 3. Debug a Container

```bash
# SSH into backend
docker-compose exec backend bash

# SSH into frontend
docker-compose exec frontend sh

# Connect to database
docker-compose exec postgres psql -U myuser -d mydb
```

### 4. Clean Up Everything

```bash
# Stop and remove containers (keep data)
docker-compose down

# Stop and remove everything including database
docker-compose down -v

# Prune unused Docker resources
docker system prune -f
```

---

## Troubleshooting

### "Port Already in Use"

```bash
# Find process using port 3000
lsof -i :3000

# Kill it
kill -9 <PID>

# Or provide different port in docker-compose.yml
ports:
  - "3001:80"  # Use 3001 instead
```

### "Connection Refused" from Frontend

**Symptom:** Browser console shows connection errors to http://backend:8080

**Solution:**
```bash
# Make sure backend is healthy
docker-compose ps

# Check backend logs
docker-compose logs backend

# Test connectivity from frontend container
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health
```

### Database Won't Connect

```bash
# Check if postgres is running
docker-compose ps postgres

# View database logs
docker-compose logs postgres

# Test database from backend container
docker-compose exec backend nc -zv postgres 5432
```

### Blank Frontend Page

```bash
# Rebuild frontend with environment variables
docker-compose down -v
docker-compose up --build

# Check Nginx config is correct
docker-compose exec frontend nginx -T

# Check frontend logs
docker-compose logs frontend
```

---

## Environment Configuration

Edit `.env` to customize:

```env
# Database credentials
DB_USER=myuser
DB_PASSWORD=mypassword
DB_NAME=mydb

# Backend
HIBERNATE_DDL=create          # create, update, create-drop, validate
STRIPE_SECRET_KEY=sk_test_...

# Frontend API endpoint
VITE_BACK_END_URL=http://backend:8080
```

**Restart services to apply changes:**
```bash
docker-compose down
docker-compose up --build
```

---

## Production Deployment Notes

Before deploying to production:

1. **Update `.env` with real credentials**
   ```env
   DB_PASSWORD=very_strong_password
   STRIPE_SECRET_KEY=sk_live_actual_key
   ```

2. **Change Hibernate mode:**
   ```env
   HIBERNATE_DDL=update
   ```

3. **Add resource limits in docker-compose.yml:**
   ```yaml
   backend:
     deploy:
       resources:
         limits:
           cpus: '1'
           memory: 500M
   ```

4. **Enable HTTPS:**
   - Add SSL certificate to Nginx config
   - Update frontend API URL to HTTPS

---

## Performance Tips

### Faster Rebuilds

```bash
# Use --no-cache only when needed
docker-compose build --no-cache

# Otherwise rebuilds use cache (faster)
docker-compose build
```

### Check Service Health

```bash
./docker-scripts.sh health

# Or manually
curl http://localhost:8080/actuator/health
```

### Monitor Resource Usage

```bash
# Real-time resource usage
docker stats

# In another terminal while services run
watch -n 1 'docker stats --no-trunc'
```

---

## Next Steps

1. ✅ Run `docker-compose up --build`
2. ✅ Visit http://localhost:3000
3. ✅ Check backend at http://localhost:8080/actuator/health
4. ✅ Review `DOCKER_SETUP.md` for detailed documentation
5. ✅ Use `./docker-scripts.sh help` for more commands

**Happy coding! 🚀**
