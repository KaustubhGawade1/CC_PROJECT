# Docker Inter-Service Communication Explained

## Executive Summary

Inside Docker:
- **Frontend calls Backend** using `http://backend:8080` (service name)
- **Backend calls Database** using `jdbc:postgresql://postgres:5432` (service name)
- Docker's **internal DNS** automatically resolves service names to IP addresses
- All communication happens through the `dams_network` bridge

---

## How Docker Networks Work

### Network Types

```
┌─────────────────────────────────────────────────┐
│           Docker Networks                       │
├─────────────────────────────────────────────────┤
│                                                 │
│  1. bridge      ← Custom bridge (what we use)   │
│  2. host        ← Host network (not isolated)   │
│  3. overlay     ← For Docker Swarm (advanced)   │
│  4. none        ← No network (isolated)         │
│                                                 │
└─────────────────────────────────────────────────┘
```

**We use:** Custom bridge network (`dams_network`)

---

## Our Docker Network Architecture

```yaml
# From docker-compose.yml:
networks:
  dams_network:
    driver: bridge

services:
  postgres:
    networks:
      - dams_network    # Connected to bridge
  backend:
    networks:
      - dams_network    # Connected to bridge
  frontend:
    networks:
      - dams_network    # Connected to bridge
```

**Result:**
```
                    dams_network (bridge)
┌─────────────────────────────────────────────────┐
│  172.20.0.0/16 subnet (Docker manages IPs)     │
├─────────────────────────────────────────────────┤
│                                                 │
│  postgres container          backend container  │
│  ┌──────────────────┐        ┌────────────────┐ │
│  │ IP: 172.20.0.3   │        │ IP: 172.20.0.2 │ │
│  │ Hostname: postgres          │ Hostname: backend │
│  └──────────────────┘        └────────────────┘ │
│           ↑                           ↑          │
│           └───────────────────────────┘          │
│                                                 │
│  frontend container                             │
│  ┌────────────────────────────────────────────┐ │
│  │ IP: 172.20.0.4                             │ │
│  │ Hostname: frontend                         │ │
│  │ Can reach: backend:8080, postgres:5432    │ │
│  └────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## Service Discovery (DNS Resolution)

### How "backend" hostname resolves

```
Frontend Container
    ↓
Request to: http://backend:8080/api/users
    ↓
Docker's Embedded DNS Server (127.0.0.11:53)
    ↓
DNS Query: "What IP is 'backend'?"
    ↓
Docker DNS Resolution:
  - Checks docker-compose.yml service names
  - Finds "backend" service in dams_network
  - Returns 172.20.0.2 (backend container's internal IP)
    ↓
Connection established to 172.20.0.2:8080
    ↓
Backend Container receives request
```

### Key Point: Service Name = Hostname

```yaml
services:
  backend:           # ← This becomes the hostname
    container_name: dams_backend
    
# Inside other containers:
http://backend:8080        # ✓ Works (service name)
http://dams_backend:8080   # ✗ May not work in bridge mode
http://localhost:8080      # ✗ Refers to the container itself
http://172.20.0.2:8080     # ✓ Works (IP address)
```

---

## Communication Flows

### Flow 1: Frontend → Backend

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  User clicks button in browser at localhost:3000           │
│                                                             │
│  React code (from api.js):                                 │
│  ┌────────────────────────────────────────────────────┐    │
│  │ axios.create({                                     │    │
│  │   baseURL: 'http://backend:8080/api'  ← Service   │    │
│  │ })                                      hostname   │    │
│  └────────────────────────────────────────────────────┘    │
│                          ↓                                  │
│  Browser Fetch Request:                                    │
│  GET http://backend:8080/api/users                        │
│                          ↓                                  │
│  Docker Embedded DNS (inside frontend container):          │
│  "Resolve 'backend' hostname"                             │
│                          ↓                                  │
│  DNS Answer: 172.20.0.2 (backend container IP)            │
│                          ↓                                  │
│  HTTP Request to 172.20.0.2:8080/api/users               │
│                          ↓                                  │
│  ┌─────────────────────────────────────────────────┐      │
│  │  Backend Container (Spring Boot)                │      │
│  │  Listening on 0.0.0.0:8080                      │      │
│  │  Receives request, processes, sends response    │      │
│  └─────────────────────────────────────────────────┘      │
│                          ↓                                  │
│  Response sent back to frontend container                 │
│                          ↓                                  │
│  Browser receives JSON data                               │
│  React updates UI                                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Flow 2: Backend → Database

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  Backend Application (Hibernate/Spring Data)            │
│                                                          │
│  Java Code:                                             │
│  ┌────────────────────────────────────────────────┐     │
│  │ String dbUrl = "jdbc:postgresql://postgres:"  │     │
│  │                         ↑         ↑           │     │
│  │                    Service name in              │     │
│  │                    docker-compose.yml          │     │
│  └────────────────────────────────────────────────┘     │
│                          ↓                               │
│  DatabaseConnection conn = DriverManager                │
│    .getConnection(dbUrl)                               │
│                          ↓                               │
│  OS-level DNS Resolution (in backend container):         │
│  "Resolve 'postgres' hostname"                          │
│                          ↓                               │
│  Docker Embedded DNS: Returns 172.20.0.3               │
│                          ↓                               │
│  TCP Connection to 172.20.0.3:5432                      │
│                          ↓                               │
│  PostgreSQL Server (in database container)              │
│  Receives connection, authenticates, opens session      │
│                          ↓                               │
│  SQL Queries sent over established connection           │
│  SELECT * FROM users;                                   │
│                          ↓                               │
│  Results returned to backend                            │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## Port Mapping: Host ↔ Container

### Port Mapping in docker-compose.yml

```yaml
frontend:
  ports:
    - "3000:80"     # Host Port : Container Port
         ↑   ↑
       Host  |
            Container
```

```
Host Machine (Your Computer)     Bridge Network (Docker Internal)
┌─────────────────────────        ┌──────────────────────────
│ Port 3000 (listening)  ━━━━━━→  │ Frontend Container
│ Port 8080 (listening)  ━━━━━━→  │ Backend Port 8080
│ Port 5432 (listening)  ━━━━━━→  │ Postgres Port 5432
└─────────────────────────        └──────────────────────────
```

### From Host Machine

```
http://localhost:3000       ← Port mapping 3000:80
   ↓
Docker forwards to container port 80
   ↓
Nginx (inside frontend container)
   ↓
Serves React app
```

### From Inside Container (Frontend to Backend)

```
Inside frontend container, it CANNOT call:
  http://localhost:8080
  (localhost = the frontend container itself)

It MUST call:
  http://backend:8080
  (backend = Docker DNS resolves to backend container)
```

---

## Health Checks & Startup Order

### Dependency Management

```yaml
services:
  backend:
    depends_on:
      postgres:
        condition: service_healthy    # Wait for DB health check

  frontend:
    depends_on:
      - backend                       # Wait for backend to start
```

**Sequence:**

```
1. PostgreSQL starts
   ↓
   Runs health check: pg_isready
   ↓ (every 10s, up to 5 times)
   Status: HEALTHY ✓

2. Backend starts
   ↓
   Waits for postgres to be HEALTHY
   ↓
   Connects to jdbc:postgresql://postgres:5432/mydb
   ↓
   Spring Boot initializes (40s startup time)
   ↓
   Runs health check: http://localhost:8080/actuator/health
   ↓
   Status: UP ✓

3. Frontend starts
   ↓
   Waits for backend to start
   ↓
   Nginx serves React app
   ↓
   React code calls http://backend:8080/api/...
   ↓
   Request successfully reaches backend
```

---

## Environment Variables in docker-compose.yml

### Backend Configuration

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mydb
                                          ↑
                                   Service name (container to container)
    
    # From outside Docker:
    # jdbc:postgresql://localhost:5432/mydb
    #                   ↑
    #                   Different! (localhost = host machine)
```

### Frontend Configuration

```yaml
frontend:
  environment:
    VITE_BACK_END_URL: http://backend:8080
                            ↑
                      Service name inside Docker
                      
    # Docker-to-container communication uses service names
    # Browser-to-service uses localhost/host ports
```

---

## Network Isolation

### Why Can't Frontend Call Localhost Backend?

```
Frontend Container                    Host Machine
┌──────────────────┐                ┌──────────────────┐
│                  │                │                  │
│  nodejs process  │                │  Your computer   │
│                  │                │                  │
│  localhost:3000  │ ╳ NO ACCESS    │  localhost:3000  │
│  localhost:8080  │ ╳ NO ACCESS    │  localhost:8080  │
│                  │                │                  │
│  But CAN access: │                │                  │
│  backend:8080    │  ← Service name│  (these are      │
│  postgres:5432   │    resolution  │   connected)     │
│                  │                │                  │
└──────────────────┘                └──────────────────┘
         │                                   │
         └───────────→ Bridge Network ←─────┘
                    (dams_network)
```

**Solution:** Always use service names inside containers:
- ✓ `http://backend:8080` (right)
- ✗ `http://localhost:8080` (wrong)

---

## Debugging Network Issues

### Test Frontend → Backend Connection

```bash
# From inside frontend container
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health

# Expected: JSON response with status
# Error: Connection refused → backend not running/healthy
```

### Test Backend → Database Connection

```bash
# From inside backend container
docker-compose exec backend nc -zv postgres 5432

# Expected: "Connection to postgres 5432 port [tcp/*] succeeded!"
# Error: Connection refused → postgres not ready
```

### View Container Network

```bash
# Inspect the network
docker network inspect cc_project_dams_network

# Shows all connected containers and their IPs
# Example output:
# [
#   {
#     "Name": "dams_postgres",
#     "IPv4Address": "172.20.0.3/16"
#   },
#   {
#     "Name": "dams_backend",
#     "IPv4Address": "172.20.0.2/16"
#   }
# ]
```

### DNS Resolution Test

```bash
# From frontend container
docker-compose exec frontend nslookup backend

# Expected: Name resolution successful
# Returns: 172.20.0.2 (or similar)

# If nslookup not available:
docker-compose exec frontend ping backend

# Expected: Ping responses from backend container
```

---

## Common Issues & Explanations

### Issue 1: Frontend Page Loads but API Calls Fail

**What happens:**
```
1. Browser loads http://localhost:3000 ✓ (Nginx serves it)
2. Frontend JavaScript runs ✓
3. React code calls http://localhost:8080/api/users ✗ (FAILS)
   ↓
   localhost = the frontend container itself (not backend)
   Frontend doesn't have API server on :8080
   Connection refused error
```

**Solution:**
```javascript
// WRONG:
axios.baseURL = 'http://localhost:8080/api'

// CORRECT (in docker-compose environment):
axios.baseURL = 'http://backend:8080/api'
```

### Issue 2: Backend Can't Connect to Database

**What happens:**
```
1. Backend tries: jdbc:postgresql://localhost:5432/mydb
   ↓
   localhost = backend container itself (not database)
   Backend container doesn't have postgres running on :5432
   Connection refused
```

**Solution:**
```java
// From application.properties (with Docker)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres:5432/mydb}
                                                                  ↑
                                                            Service name
```

### Issue 3: Still Wrong Hostname After Fixing Code

**Why it happens:**
```
1. You fix the code locally: localhost → backend
2. You commit changes
3. BUT docker-compose built image with OLD code
4. Docker uses cached image, doesn't rebuild
```

**Solution:**
```bash
# Force rebuild with --build
docker-compose down -v
docker-compose up --build

# Or rebuild specific service
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

---

## Summary Table

| Need | Inside Docker | Outside Docker | Notes |
|------|---------------|----------------|-------|
| Frontend to Backend | `http://backend:8080` | `http://localhost:8080` | Service name vs localhost |
| Backend to DB | `jdbc:postgresql://postgres:5432` | `jdbc:postgresql://localhost:5432` | Service name vs localhost |
| Frontend from browser | N/A | `http://localhost:3000` | Host port mapping |
| Database from device | N/A | `localhost:5432` | Host port mapping |

---

## Key Takeaways

1. **Service Names are DNS Hostnames** - `backend`, `postgres`, `frontend` resolve to container IPs
2. **Port Mapping Only for Host** - `3000:80` means host sees port 3000, container uses 80
3. **`localhost` is Relative** - Inside container, `localhost` = that container only
4. **Always Use Service Names Inside** - Frontend/Backend/DB use service names to talk
5. **Docker DNS is Magic** - Docker's embedded DNS automatically resolves service names
6. **Health Checks Matter** - Ensures services are ready before dependent services start
7. **Rebuild When Code Changes** - Docker uses cached images if nothing in Dockerfile changes

---

**Next:** Review [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed setup and troubleshooting guides.
