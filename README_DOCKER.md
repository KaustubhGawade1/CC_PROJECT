# DAMS Docker Documentation Index

## What's Been Set Up

Your DAMS project now has a complete Docker setup with:

✅ **Root docker-compose.yml** - Orchestrates backend, frontend, and database  
✅ **Configuration files** - Environment variables and optimization  
✅ **Helper scripts** - Easy commands for starting/stopping services  
✅ **Comprehensive documentation** - Everything explained in detail  

---

## Getting Started (Choose Your Path)

### 🚀 The Fastest Way (60 seconds)

```bash
cd /home/kaustubh/projects/CC_Project
docker-compose up --build
```

Then open **http://localhost:3000**

**Done!** Services will be ready in ~45 seconds.

---

### 📖 Want to Understand First?

Read in this order:

1. **[QUICKSTART.md](QUICKSTART.md)** (5 min read)
   - 30-second startup
   - Common tasks
   - Basic troubleshooting

2. **[DOCKER_SETUP.md](DOCKER_SETUP.md)** (15 min read)
   - Complete setup guide
   - Configuration options
   - Common mistakes explained

3. **[DOCKER_NETWORKING.md](DOCKER_NETWORKING.md)** (10 min read)
   - How services talk to each other
   - Why `backend:8080` instead of `localhost:8080`
   - Visual diagrams of communication

4. **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** (reference)
   - When something goes wrong
   - Diagnosis flowchart
   - Solutions by error type

---

## Project Structure

```
/home/kaustubh/projects/CC_Project/
├── docker-compose.yml                  # Main orchestration file
├── .env                                # Environment variables
├── docker-scripts.sh                   # Helper commands (see below)
│
├── QUICKSTART.md                       # Get running in 60 seconds
├── DOCKER_SETUP.md                     # Complete setup guide  
├── DOCKER_NETWORKING.md                # How services communicate
├── TROUBLESHOOTING.md                  # Fix issues
│
├── dams-backend/                       # Spring Boot API
│   ├── Dockerfile                      # Multi-stage build
│   └── src/main/resources/
│       └── application.properties      # Updated to use Docker DNS
│
└── front-dams-main/                    # React + Nginx
    ├── Dockerfile                      # Multi-stage build
    ├── nginx.conf                      # SPA routing config
    └── src/api/
        └── api.js                      # Uses VITE_BACK_END_URL
```

---

## What's Different from Local Development

### Before Docker (Local Development)

```javascript
// Frontend
axios.create({ baseURL: 'http://localhost:8080/api' })

// Backend
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
```

### Inside Docker

```javascript
// Frontend
axios.create({ baseURL: 'http://backend:8080/api' })
                              ↑ Service name

// Backend
spring.datasource.url=jdbc:postgresql://postgres:5432/mydb
                                       ↑ Service name
```

**Why?** Service names resolve to internal Docker IPs through DNS. See [DOCKER_NETWORKING.md](DOCKER_NETWORKING.md) for full explanation.

---

## Key Files Created/Modified

### Created Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Orchestrates all services, volumes, networks |
| `.env` | Environment variables for all services |
| `docker-scripts.sh` | Helper commands (start, stop, logs, debug) |
| `QUICKSTART.md` | 60-second startup guide |
| `DOCKER_SETUP.md` | Complete setup documentation |
| `DOCKER_NETWORKING.md` | How Docker networking works |
| `TROUBLESHOOTING.md` | Diagnosis and solutions |
| `.dockerignore` (backend) | Optimize build context |

### Modified Files

| File | Changes |
|------|---------|
| `dams-backend/src/main/resources/application.properties` | Use environment variables for database URL |
| `dams-backend/Dockerfile` | Already had multi-stage build, kept as-is |
| `front-dams-main/Dockerfile` | Added build argument for VITE_BACK_END_URL |
| `front-dams-main/nginx.conf` | Minor optimizations for Docker |

---

## Quick Reference Commands

### Using docker-compose directly

```bash
# Start everything
docker-compose up --build

# Start in background  
docker-compose up -d --build

# Stop everything
docker-compose stop

# Stop and remove
docker-compose down

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend

# Access container shell
docker-compose exec backend bash
docker-compose exec frontend sh
```

### Using helper script

```bash
# Make executable
chmod +x docker-scripts.sh

# Start services
./docker-scripts.sh start              # Foreground
./docker-scripts.sh start-bg           # Background

# Stop/clean
./docker-scripts.sh stop
./docker-scripts.sh down

# Diagnostics
./docker-scripts.sh logs               # View logs
./docker-scripts.sh health             # Check health
./docker-scripts.sh status             # Show status

# Rebuild
./docker-scripts.sh rebuild            # Everything
./docker-scripts.sh rebuild-backend    # Backend only
./docker-scripts.sh rebuild-frontend   # Frontend only

# Access containers
./docker-scripts.sh shell-backend      # Bash in backend
./docker-scripts.sh shell-frontend     # Shell in frontend
./docker-scripts.sh shell-db           # psql in database
```

---

## Service Details

### PostgreSQL Database
- **Port:** 5432
- **Container Name:** dams_postgres
- **Credentials:** myuser / mypassword (in `.env`)
- **Database:** mydb
- **Data Location:** `postgres_data` volume (persistent)
- **Health Check:** Every 10 seconds

### Spring Boot Backend
- **Port:** 8080
- **Container Name:** dams_backend
- **Database Connection:** `jdbc:postgresql://postgres:5432/mydb`
- **Health Check:** Every 30 seconds at `/actuator/health`
- **Startup Time:** ~40 seconds
- **Memory:** 300MB (configurable via JAVA_TOOL_OPTIONS)

### React Frontend (with Nginx)
- **Port:** 3000
- **Container Name:** dams_frontend
- **Backend URL:** `http://backend:8080` (inside Docker)
- **SPA Routing:** Configured for client-side navigation
- **Build Tool:** Vite
- **Server:** Nginx

---

## Environment Variables (.env)

```env
# Database Configuration
DB_USER=myuser                          # PostgreSQL user
DB_PASSWORD=mypassword                  # PostgreSQL password
DB_NAME=mydb                            # Database name

# Backend Configuration
HIBERNATE_DDL=create                    # create|update|create-drop|validate
STRIPE_SECRET_KEY=sk_test_...          # Add your actual key

# Frontend Configuration
VITE_BACK_END_URL=http://backend:8080   # Backend API endpoint
```

**To customize:** 
- Edit `.env` file
- Restart services: `docker-compose down && docker-compose up --build`

---

## How Communication Works (Simplified)

### Frontend → Backend

```
User clicks button in browser
    ↓
React calls http://backend:8080/api/users
    ↓
Docker DNS resolves "backend" → 172.20.0.2 (backend container IP)
    ↓
Request reaches backend service
    ↓
Backend processes and responds
```

### Backend → Database

```
Spring Boot initializes
    ↓
Connects to jdbc:postgresql://postgres:5432/mydb
    ↓
Docker DNS resolves "postgres" → 172.20.0.3 (database container IP)
    ↓
Connection established, queries execute
```

**Full explanation:** See [DOCKER_NETWORKING.md](DOCKER_NETWORKING.md)

---

## Common Issues & Quick Fixes

| Issue | Quick Fix |
|-------|-----------|
| "Port already in use" | `kill -9 $(lsof -t -i :3000)` or use different port |
| Blank frontend page | `docker-compose down -v && docker-compose up --build` |
| API calls fail (localhost) | Change `localhost:8080` to `backend:8080` in frontend |
| Database connections fail | Restart: `docker-compose down -v && docker-compose up --build` |
| Services won't start | Check logs: `docker-compose logs` |

**Full troubleshooting:** See [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

---

## Next Steps

### 1. Start Services

```bash
cd /home/kaustubh/projects/CC_Project
docker-compose up --build
```

### 2. Verify Everything Works

```bash
# In another terminal
./docker-scripts.sh status

# Or visit in browser:
http://localhost:3000        # Frontend
http://localhost:8080        # Backend health
```

### 3. Make Code Changes

- Edit backend code → runs in container
- Edit frontend code → need to rebuild

```bash
# Rebuild specific service
docker-compose build --no-cache frontend
docker-compose up -d frontend

# Or rebuild all
docker-compose up --build
```

### 4. Debug Issues

```bash
# Check logs
docker-compose logs -f

# Access container
docker-compose exec backend bash

# Test connectivity
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health
```

---

## Important Notes

✅ **Services are connected:** All services on same `dams_network`, can reach each other  
✅ **Data persists:** PostgreSQL data saved in `postgres_data` volume  
✅ **Health checks:** Services wait for dependencies to be ready  
✅ **Environment variables:** Loaded from `.env` file, configurable  
✅ **Optimized builds:** Multi-stage builds keep images small  
✅ **Production-ready:** Includes restart policies, health checks, volume management  

❌ **Don't:** Use `localhost` inside containers (use service names instead)  
❌ **Don't:** Use `-v` flag with `docker-compose down` if you want to keep database  
❌ **Don't:** Edit Dockerfile without rebuilding: `docker-compose up --build`  

---

## Recommended Reading Order

1. **First time?** →  [QUICKSTART.md](QUICKSTART.md)
2. **Want details?** → [DOCKER_SETUP.md](DOCKER_SETUP.md)
3. **Something broken?** → [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
4. **Curious about networking?** → [DOCKER_NETWORKING.md](DOCKER_NETWORKING.md)

---

## Files at a Glance

### docker-compose.yml
Main orchestration file that:
- Defines PostgreSQL, Backend, Frontend services
- Sets up network communication
- Manages volumes for data persistence
- Loads environment variables from `.env`

### .env
Configuration file with:
- Database credentials
- Backend settings (Hibernate DDL mode, Stripe key)
- Frontend API endpoint

### docker-scripts.sh
Helper script providing:
- Easy start/stop commands
- Health checks
- Log viewing
- Container access
- Rebuild options

### QUICKSTART.md
Getting running fast:
- 30-second startup
- Common tasks
- Basic troubleshooting

### DOCKER_SETUP.md
Complete guide:
- Configuration options
- Common mistakes & solutions
- Useful commands
- Production considerations

### DOCKER_NETWORKING.md
Understanding Docker networks:
- How services communicate
- Service name resolution
- Network architecture
- Debugging network issues

### TROUBLESHOOTING.md
Fixing problems:
- Diagnosis flowchart
- Solutions by error type
- Network debugging
- Performance issues

---

## Support Resources

| Need | Reference |
|------|-----------|
| Quick start | [QUICKSTART.md](QUICKSTART.md) |
| Full setup | [DOCKER_SETUP.md](DOCKER_SETUP.md) |
| Understanding communication | [DOCKER_NETWORKING.md](DOCKER_NETWORKING.md) |
| Fixing issues | [TROUBLESHOOTING.md](TROUBLESHOOTING.md) |
| Commands | Run `./docker-scripts.sh help` |

---

## Last Tips

1. **Always check logs first:** `docker-compose logs -f`
2. **Use the helper script:** `./docker-scripts.sh start-bg` then `./docker-scripts.sh logs`
3. **Test connectivity:** `docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health`
4. **Rebuild when code changes:** `docker-compose build --no-cache`
5. **For development:** Use background mode: `./docker-scripts.sh start-bg`

---

**You're all set! 🚀 Start with:** `docker-compose up --build`
