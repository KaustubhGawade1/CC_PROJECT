# Port Configuration Update - April 6, 2026

## Issue Resolved

**Problem:** Port 5433 was held by a background process on the host system, preventing Docker containers from starting.

**Solution:** Changed external port mappings to avoid conflicts:

| Service | Old Port | New Port | Internal Port |
|---------|----------|----------|---------------|
| PostgreSQL | 5433 | **5435** | 5432 (unchanged) |
| Backend | 8080 | **8081** | 8080 (unchanged) |
| Frontend | 3000 | **3001** | 80 (unchanged) |

## Current Status

✅ **All services running:**
- Frontend Nginx: http://localhost:3001
- Backend Spring Boot: http://localhost:8081
- PostgreSQL: localhost:5435

✅ **API Working:** Nginx is correctly proxying `/api/*` requests to backend

✅ **Frontend Serving:** React SPA loading successfully from Nginx

## Testing

### Test Frontend
```bash
# Open in browser or curl
curl http://localhost:3001

# Expected: Vite + React HTML page loads
```

### Test API Proxy
```bash
# Test HTTP endpoint (will get 401 Unauthorized - expected)
curl http://localhost:3001/api/public/asset

# Test database connectivity
curl http://localhost:3001/api/health  # if you have a health endpoint
```

### Test From Browser
1. Open http://localhost:3001 in your browser
2. DevTools → Network tab
3. Try login/signup
4. Verify requests show `/api/...` paths
5. Check that Nginx is proxying (no CORS errors)

### Test File Operations
1. Navigate to "Add Asset"
2. Upload a file
3. Verify upload endpoint: `/api/asset/add/file`
4. Check response contains filePath

### Test Image Rendering
1. Browse assets
2. Verify images load correctly
3. Inspect image src in DevTools: should be `/<filePath>` format
4. Download button should work

## Next Steps

### Option 1: Keep Using These Ports (5435, 8081, 3001)
This is recommended if ports 5433, 8080, 3000 are needed for other services:

```bash
# Keep current docker-compose.yml as-is
# Always access at http://localhost:3001 (not 3000)
```

### Option 2: Find & Kill Process Using Original Ports

To free up the original ports:

```bash
# Check what's using port 5433
sudo netstat -tulpn | grep 5433
# or
sudo lsof -i :5433

# Kill the process if needed
sudo kill -9 <PID>

# Revert docker-compose.yml ports back to:
# PostgreSQL: 5433:5432
# Backend: 8080:8080
# Frontend: 3000:80

# Restart services
docker compose down && docker compose up -d --build
```

### Option 3: Uninstall Conflicting Services

If you have PostgreSQL, MySQL, or other services running on the host:

```bash
# List installed services (example for PostgreSQL)
sudo systemctl list-units --type=service --state=running | grep -i postgres

# Stop the service
sudo systemctl stop postgresql
sudo systemctl stop mysql
# etc.

# Then you can revert to original ports
```

## Architecture Confirmation

The new relative API paths architecture is now confirmed working:

```
Browser (http://localhost:3001)
    ↓
Nginx Container (Port 3001 → 80 internal)
    ├─ Static assets (React SPA)
    └─ /api/* → proxy_pass to backend:8081/api/
         ↓
Backend Container (Port 8081 → 8080 internal)
    ├─ API endpoints (/api/*)
    └─ File service (/uploads/*)
         ↓
PostgreSQL Container (Port 5435 → 5432 internal)
```

## Configuration Files Updated

- [docker-compose.yml](docker-compose.yml) - Port mappings changed
- All other configurations remain the same (Dockerfile, nginx.conf, etc.)

## Important Notes

1. **External vs Internal Ports:** Only the external (host-facing) ports changed. Internal Docker networking remains on standard ports.

2. **No Code Changes Needed:** Backend code doesn't need to change (it still runs on 8080 internally).

3. **Relative API Paths:** Frontend continues using `/api` - works on any port.

4. **Persistence:** Port changes only affect CURRENT session. If you restart, ensure this docker-compose.yml is used.

5. **EC2 Deployment:** Won't be affected by these local port changes. Use original ports on EC2 (or adjust as needed).

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Address already in use" | Use different external ports (like 5435, 8081, 3001) |
| API returning 502 | Check backend container: `docker logs dams_backend` |
| CORS errors | Verify backend's `APP_CORS_ALLOWED_ORIGINS` or check frontend origin |
| Images not loading | Check image `src` attributes in browser DevTools |
| Nginx not responding | Check Nginx: `docker logs dams_frontend` |

## Quick Commands

```bash
# View logs
docker logs dams_frontend
docker logs dams_backend
docker logs dams_postgres

# Restart a service
docker restart dams_frontend
docker restart dams_backend

# Full restart
docker compose down && docker compose up -d

# Check port usage on host
netstat -tulpn | grep -E ":(5435|8081|3001)"
```

---

**Status:** ✅ All services running with relative API paths working correctly  
**Date:** April 6, 2026  
**Environment:** Local Docker development
