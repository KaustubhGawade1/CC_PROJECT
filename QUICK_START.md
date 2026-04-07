# Quick Start Cheat Sheet

## Current Setup (April 6, 2026)

### Access URLs
```
Frontend:   http://localhost:3001
Backend:    http://localhost:8081
Database:   localhost:5435
```

### Start Services
```bash
cd /home/kaustubh/projects/CC_Project
docker compose up -d --build
```

### Stop Services
```bash
cd /home/kaustubh/projects/CC_Project
docker compose down
```

### View Logs
```bash
docker logs dams_frontend   # Frontend/Nginx
docker logs dams_backend    # Spring Boot API
docker logs dams_postgres   # PostgreSQL
docker logs -f dams_backend # Follow backend logs
```

### Test API
```bash
# Through Nginx reverse proxy (from frontend)
curl http://localhost:3001/api/public/asset

# Direct to backend
curl http://localhost:8081/api/public/asset

# Database connection
docker exec dams_postgres psql -U myuser -d mydb -c "SELECT 1;"
```

### Check Container Status
```bash
docker ps                    # Running containers
docker ps -a                 # All containers
docker compose ps            # Detailed status
```

### Service Ports

| Service | Host Port | Container Port | Status |
|---------|-----------|-----------------|--------|
| Frontend (Nginx) | 3001 | 80 | ✅ Running |
| Backend (Spring) | 8081 | 8080 | ✅ Running |
| PostgreSQL | 5435 | 5432 | ✅ Running |

### Architecture

```
User Browser (http://localhost:3001)
    ↓
Nginx (reverse proxy + static serving)
    ├─ Frontend files → React SPA
    └─ /api/* → http://backend:8080
    ↓
Backend Spring Boot (http://backend:8081 internally)
    ├─ API Endpoints (/api/*)
    └─ File uploads (/uploads/*)
    ↓
PostgreSQL (postgres:5432 internally)
```

### Database Info
- **Host:** localhost:5435
- **User:** myuser (default)
- **Password:** mypassword (default)
- **Database:** mydb (default)
- **Connection String:** `jdbc:postgresql://postgres:5432/mydb` (internal Docker)

### API Endpoints (with relative paths)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/public/asset` | List assets |
| POST | `/api/auth/signin` | Login |
| POST | `/api/auth/signup` | Register |
| POST | `/api/asset/add/file` | Upload file |
| DELETE | `/api/asset/{id}` | Delete asset |

### Frontend Testing

1. **Open:** http://localhost:3001
2. **Register:** Click "Sign Up" → Fill form → Submit
3. **Login:** Click "Log In" → Enter credentials → Submit
4. **Upload:** Navigate to "Add Asset" → Choose file → Upload
5. **Browse:** View uploaded assets in main feed

### DevTools Debugging

**Check Network Requests:**
1. Open http://localhost:3001 in browser
2. Press F12 → Network tab
3. Try login/signup
4. Verify requests show:
   - `POST /api/auth/signin` (not `POST http://...`)
   - `GET /api/public/asset` (not `GET http://...`)
   - Headers include `Authorization: Bearer <token>`

**Check Console:**
- Should see no CORS errors
- Should see successful API responses
- Check localStorage: `authToken` and `auth` stored

### Common Issues

| Problem | Check |
|---------|-------|
| "Cannot connect" | `docker ps` - are containers running? |
| "Connection refused" | Port changed? Check `docker-compose.yml` |
| API 401 error | Normal - auth required. Try signup/login. |
| Images not loading | Check browser Network tab - verify paths |
| CORS error | Check backend's `APP_CORS_ALLOWED_ORIGINS` |

### Environment Variables (Backend)

Edit `.env` or `.env.prod`:
```
# Database
DB_USER=myuser
DB_PASSWORD=mypassword
DB_NAME=mydb

# CORS (must include frontend origin if on different IP)
APP_CORS_ALLOWED_ORIGINS=http://localhost:3001,http://localhost:5173

# Hibernate
HIBERNATE_DDL=create

# Stripe
STRIPE_SECRET_KEY=sk_test_...
```

### Docker Compose Override (if needed)

To test different ports without editing docker-compose.yml:

```bash
docker compose -p myapp -f docker-compose.yml up -d
```

### Rebuild Components Only

```bash
# Rebuild frontend
docker compose build frontend
docker compose up -d frontend

# Rebuild backend
docker compose build backend
docker compose up -d backend

# Rebuild specific service (with new code)
docker compose up -d --build backend
```

### Reset Everything

```bash
# Stop and remove all containers
docker compose down -v --remove-orphans

# Remove all images
docker system prune -a

# Start fresh
docker compose up -d --build
```

### Performance Monitoring

```bash
# Check container resource usage
docker stats

# View container details
docker inspect dams_backend

# Check disk space
docker system df
```

---

**Last Updated:** April 6, 2026  
**Relative API Paths:** ✅ Enabled  
**All Services:** ✅ Running
