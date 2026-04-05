# Docker Setup Guide - DAMS Project

This document explains how to run the DAMS project (Backend + Frontend) using Docker Compose on your local machine.

## Project Structure

```
CC_PROJECT/
├── docker-compose.yml          # Root compose file (orchestrates all services)
├── .env                         # Environment variables
├── dams-backend/               # Spring Boot backend
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── application.properties
└── front-dams-main/            # React + Vite + Nginx frontend
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    └── vite.config.js
```

---

## Prerequisites

1. **Docker & Docker Compose** installed on your machine
   - [Install Docker Desktop](https://www.docker.com/products/docker-desktop)
   - Verify: `docker --version && docker-compose --version`

2. **Port Availability**
   - Port 3000 (Frontend Nginx)
   - Port 8080 (Backend Spring Boot)
   - Port 5432 (PostgreSQL Database)

---

## Quick Start

### Step 1: Start All Services

```bash
cd /home/kaustubh/projects/CC_Project

# Start all services
docker compose up -d

# Watch logs (optional)
docker compose logs -f
```

**Note:** First startup takes ~60 seconds as services initialize.

**Expected Output:**
```
[+] Running 4/4
 ✓ Container dams_postgres  Healthy
 ✓ Container dams_backend   Started (Running health check...)
 ✓ Container dams_frontend  Started
```

### Step 2: Access Services

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | http://localhost:3000 | React app (SPA) |
| **Backend API** | http://localhost:8080/api | Spring Boot API |
| **Database** | localhost:5433 | PostgreSQL |

### Step 3: Stop All Services

```bash
docker-compose down

# Also remove volumes (database data)
docker-compose down -v
```

---

## Configuration

### Environment Variables (.env file)

The `.env` file sets defaults for all services:

```env
# Database Configuration
DB_USER=myuser
DB_PASSWORD=mypassword
DB_NAME=mydb

# Backend Configuration
HIBERNATE_DDL=create          # Options: create, update, create-drop, validate
STRIPE_SECRET_KEY=sk_test_...

# Frontend Configuration
VITE_BACK_END_URL=http://backend:8080
```

#### Customizing Configuration

**Option 1: Edit `.env` file**
```env
DB_PASSWORD=my_secure_password_123
HIBERNATE_DDL=update
```

**Option 2: Override via command line**
```bash
DB_USER=customuser docker-compose up --build
```

**Option 3: Use different .env file**
```bash
--env-file .env.production docker-compose up
```

---

## How Docker Networking Works

### Service-to-Service Communication

Inside Docker, services communicate using their container names as hostnames. The compose file creates a custom bridge network (`dams_network`).

```
Frontend (React) → Nginx (port 3000)
                 ↓
              Nginx proxy rules
           (try_files, SPA routing)
                 ↓
   Frontend code in browser fetches from:
   http://backend:8080/api/...
                 ↓
         Backend Service DNS
      (Docker's internal DNS)
                 ↓
Backend (Spring Boot) Container
```

### Hostname Resolution

**Inside Containers (Docker DNS):**
```
backend    → resolves to 172.20.0.2 (backend container IP)
postgres   → resolves to 172.20.0.3 (postgres container IP)
frontend   → resolves to 172.20.0.4 (frontend container IP)
```

**From Host Machine:**
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Database: localhost:5432

### Communication Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Bridge Network                   │
│                      (dams_network)                          │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Frontend Container          Backend Container               │
│  ┌─────────────────┐        ┌──────────────────┐            │
│  │ Nginx (port 80) │        │ Spring Boot      │            │
│  │                 │        │ (port 8080)      │            │
│  │ React SPA code  │        │                  │            │
│  │                 │────────│ Service Name:    │            │
│  │ var: backend:   │ HTTP   │ 'backend'        │            │
│  │ 8080            │────────│                  │            │
│  └─────────────────┘        └──────────────────┘            │
│                                       ↓                       │
│                            Postgres   │                      │
│                            ┌──────────┴─────────┐           │
│                            │ Container Name:    │           │
│                            │ 'postgres'         │           │
│                            │ (port 5432)        │           │
│                            └────────────────────┘           │
│                                                               │
└──────────────────────────────────────────────────────────────┘

   External (Host Machine)
   ├─ localhost:3000   → Nginx port 3000
   ├─ localhost:8080   → Backend port 8080
   └─ localhost:5432   → Postgres port 5432
```

---

## Important Configuration Details

### Backend ↔ Database Connection

**Inside Docker:**
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mydb
                                        ↑
                                    Service name
```

**Before Docker (localhost):**
```
jdbc:postgresql://localhost:5432/mydb
```

### Frontend → Backend API Calls

**Frontend Code (src/api/api.js):**
```javascript
const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
    // VITE_BACK_END_URL = 'http://backend:8080' (from docker-compose)
});
```

**How it resolves in browser:**
1. Browser sees: `http://backend:8080/api/users`
2. Docker DNS intercepts and resolves `backend` → backend container
3. Request reaches actual backend service

---

## Common Mistakes & Solutions

### ❌ Mistake 1: Using localhost in Frontend

**Wrong:**
```javascript
axios.create({ baseURL: 'http://localhost:8080/api' })
```

**Why it fails:**
- Frontend runs inside a container
- `localhost` refers to the container itself, not the host
- Backend container is not on `localhost`

**Correct:**
```javascript
// Inside docker-compose.yml
VITE_BACK_END_URL: http://backend:8080
// backend = service name in docker-compose.yml
```

---

### ❌ Mistake 2: Frontend trying to connect before Backend is ready

**Wrong:**
```yaml
frontend:
  depends_on:
    - backend  # This only checks if container started, not if service is healthy
```

**Why it fails:**
- Spring Boot takes 30-40 seconds to start up
- Frontend tries to connect immediately
- Connection refused errors

**Correct (Already in our docker-compose.yml):**
```yaml
backend:
  healthcheck:
    test: [ "CMD", "curl", "-f", "http://localhost:8080/actuator/health" ]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s

postgres:
  healthcheck:
    test: [ "CMD-SHELL", "pg_isready -U myuser" ]
    interval: 10s
    timeout: 5s
    retries: 5

frontend:
  depends_on:
    backend:
      condition: service_healthy  # Wait for backend to be healthy
```

---

### ❌ Mistake 3: Container name conflicts

**Error:**
```
Error response from daemon: Conflict. The container name "/dams_backend" 
is already in use by container...
```

**Solutions:**

```bash
# Option 1: Stop conflicting container
docker stop dams_backend
docker rm dams_backend

# Option 2: Use --build to rebuild
docker-compose down -v
docker-compose up --build

# Option 3: Force recreate
docker-compose up --build --force-recreate
```

---

### ❌ Mistake 4: Port already in use

**Error:**
```
Error starting userland proxy: listen tcp 0.0.0.0:3000: 
bind: address already in use
```

**Solutions:**

```bash
# Find what's using port 3000
lsof -i :3000

# Kill the process (Linux/Mac)
kill -9 <PID>

# Or use a different port in docker-compose.yml
ports:
  - "3001:80"  # Host port 3001 → Container port 80
```

---

### ❌ Mistake 5: Frontend Nginx showing blank page

**Symptoms:**
- Page loads but no React app visible
- Browser console clear, no errors
- Network tab shows 200 OK for index.html

**Possible causes:**
1. Environment variable not passed to build
2. API base URL misconfigured
3. Nginx not routing SPA correctly

**Fixes:**

``` bash
# Rebuild with proper env variables
docker-compose down -v
docker-compose up --build

# Or check Nginx config is routing correctly
# location / { try_files $uri $uri/ /index.html; }
```

---

### ❌ Mistake 6: Database persistence lost

**Error:**
```
Each time I restart, the database is empty!
```

**Cause:**
- Volume not properly configured
- Using `docker-compose down -v` (the `-v` removes volumes)

**Solution:**

```bash
# Don't use -v when you want to preserve data
docker-compose down              # Keep volumes
docker-compose up --build        # Restart with data intact

# Only use -v to clear everything
docker-compose down -v           # Remove volumes too
```

---

## Useful Docker Compose Commands

```bash
# Build and start (foreground, see logs)
docker-compose up --build

# Start in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f frontend

# Check running containers
docker-compose ps

# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Remove everything including volumes
docker-compose down -v

# Execute command in container
docker-compose exec backend bash

# Rebuild only specific service
docker-compose build backend

# Force recreate containers
docker-compose up --build --force-recreate
```

---

## Troubleshooting 

### Backend not connecting to database

```bash
# Check backend logs
docker-compose logs backend

# Expected: Spring Boot should show "Started DatabasemanagementsystemApplication"
# Error: "Connection refused" means postgres not ready or wrong hostname
```

**Verify database connection:**
```bash
# Access backend container
docker-compose exec backend bash

# Test postgres connectivity
nc -zv postgres 5432

# Should output: Connection to postgres 5432 port [tcp/*] succeeded!
```

---

### Frontend can't reach backend API

```bash
# Check frontend logs
docker-compose logs frontend

# Check if Nginx is running
docker-compose exec frontend nginx -t

# Test from frontend container
docker-compose exec frontend wget -q -O- http://backend:8080/api/users
```

---

### Database not persisting after restart

```bash
# Check volume
docker volume ls | grep dams

# Inspect volume
docker volume inspect CC_Project_postgres_data

# If volume not shown, ensure docker-compose.yml has volumes section

volumes:
  postgres_data:
    driver: local
```

---

## Production Considerations

For production deployment, update `.env`:

```env
# security: Change default credentials
DB_PASSWORD=your_strong_password_here
STRIPE_SECRET_KEY=sk_live_actual_key

# Performance: Use update instead of create
HIBERNATE_DDL=update

# Logging: Set to INFO for production
```

Also update `docker-compose.yml`:
- Remove container_name for auto-generated names (better for scaling)
- Add restart: always
- Consider resource limits
- Use health checks for monitoring

---

## Next Steps

1. ✅ Start services with `docker-compose up --build`
2. ✅ Access frontend at http://localhost:3000
3. ✅ Check backend health at http://localhost:8080/actuator/health
4. ✅ Monitor logs with `docker-compose logs -f`
5. ✅ Read error messages carefully for specific issues

---

**Questions?** Check the logs first:
```bash
docker-compose logs -f
```

The logs will tell you exactly what's wrong!
