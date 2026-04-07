# Quick Reference: Relative API Paths Migration Summary

## Files Modified

| File | Changes | Reason |
|------|---------|--------|
| `src/api/api.js` | Changed baseURL from `${VITE_BACK_END_URL}/api` to `/api` | Use relative paths for Nginx reverse proxy |
| `src/pages/AddAsset.jsx` | Replaced `axios.post()` with `api.post()` and relative path | Use centralized api instance with auth interceptor |
| `src/components/shared/ProductCard.jsx` | Changed image src and download URL to relative paths | Work with Nginx reverse proxy |
| `src/components/shared/ProductViewModal.jsx` | Changed image src to relative path | Work with Nginx reverse proxy |
| `Dockerfile` | Removed `ARG VITE_BACK_END_URL` and `ENV VITE_BACK_END_URL` | No build-time config needed |
| `nginx.conf` | Added `/api/` reverse proxy block | Route API requests to backend |
| `docker-compose.yml` | Removed `VITE_BACK_END_URL` build args and environment | Simplify configuration |

## Key Architecture Changes

### Before
```
Frontend (hardcoded URL: http://backend:8080)
    ↓ knows backend location
Backend API (http://backend:8080/api)
```

### After
```
Frontend (relative path: /api)
    ↓ Nginx intercepts
Nginx (reverse proxy at /api/)
    ↓ Routes to backend
Backend API (http://backend:8080/api)
```

## Benefits
- ✅ Works locally without configuration
- ✅ Works on EC2 without URL changes
- ✅ Works behind load balancers
- ✅ Works with multiple deployment scenarios
- ✅ Simplified build process
- ✅ Centralized API instance in all files

## Testing Checklist

```bash
# 1. Build and start services
docker compose down && docker compose up -d --build

# 2. Check frontend Nginx is running
docker logs dams_frontend

# 3. Test API from browser
# Open http://localhost:3000
# Check DevTools Network tab for /api requests
# Verify all requests succeed

# 4. Test specific endpoints
curl -X GET http://localhost:3000/api/public/asset

# 5. Test authentication
# Try login/signup in UI

# 6. Test file upload
# Upload a file in "Add Asset" page
```

## Environment Variables

**Frontend:**
- ❌ `VITE_BACK_END_URL` - NO LONGER NEEDED

**Backend (still required):**
- ✅ `APP_CORS_ALLOWED_ORIGINS` - Must include frontend origin for CORS

## Rollback (if needed)

If you need to revert to hardcoded URLs:
1. Restore original versions from git
2. Update Dockerfile with `ARG VITE_BACK_END_URL=http://backend:8080`
3. Update all component files with `${import.meta.env.VITE_BACK_END_URL}`
4. Revert docker-compose.yml changes

## Production Deployment Notes

### EC2 with Separate Instances
- Frontend EC2 runs Nginx (this frontend)
- Backend EC2 runs Spring Boot
- Update backend's `APP_CORS_ALLOWED_ORIGINS` to include frontend IP
- Frontend doesn't need any configuration changes

### Behind ALB (Application Load Balancer)
- ALB routes traffic on port 80/443 to frontend Nginx
- Nginx reverse proxy routes /api to backend (internal)
- CORS not needed (same origin from browser perspective)

### With Custom Domain
- Frontend served at `app.example.com`
- Backend at `api.example.com` or `app.example.com/api`
- Nginx reverse proxy handles this transparently if on same Docker network

## Files Reference

- **Main Changes:** Related to API communication
  - [src/api/api.js](src/api/api.js)
  - [src/pages/AddAsset.jsx](src/pages/AddAsset.jsx)
  - [src/components/shared/ProductCard.jsx](src/components/shared/ProductCard.jsx)
  - [src/components/shared/ProductViewModal.jsx](src/components/shared/ProductViewModal.jsx)

- **Configuration Changes:**
  - [Dockerfile](Dockerfile)
  - [nginx.conf](nginx.conf)
  - [docker-compose.yml](../docker-compose.yml)

- **Documentation:**
  - [RELATIVE_API_PATHS.md](RELATIVE_API_PATHS.md) - Detailed migration guide
  - [.env.example](.env.example) - Example environment variables

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| 404 on `/api/endpoint` | Check Nginx config syntax: `docker exec dams_frontend nginx -t` |
| CORS errors | Verify backend `APP_CORS_ALLOWED_ORIGINS` includes frontend origin |
| File download fails | Verify filePath from API response starts with `/uploads/` |
| Images not loading | Check browser Network tab; verify proxy is working |
| 502 Bad Gateway | Ensure backend container is running: `docker compose ps` |
