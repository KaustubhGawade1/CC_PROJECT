# Relative API Paths Migration

## Overview

The React (Vite) frontend has been updated to use **relative API paths** (`/api`) instead of hardcoded backend URLs. This enables seamless deployment across different environments (local Docker, EC2, cloud platforms) without environment-specific configuration.

## Changes Made

### 1. **API Service Layer** (`src/api/api.js`)

**Before:**
```javascript
const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
    withCredentials: true,
});
```

**After:**
```javascript
const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
});
```

**Why:** Using relative paths allows Nginx reverse proxy to intercept and route `/api/` requests to the backend service without frontend knowledge of the backend's actual location.

### 2. **File Upload Component** (`src/pages/AddAsset.jsx`)

**Before:**
```javascript
const response = await axios.post(
    `${import.meta.env.VITE_BACK_END_URL}/api/asset/add/file`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
        Authorization: `Bearer ${pureToken}`,
      },
    }
);
```

**After:**
```javascript
// Removed hardcoded axios import; now uses centralized api instance
const response = await api.post(
    `/asset/add/file`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
);
```

**Benefits:**
- ✅ Automatically includes Authorization header via interceptor
- ✅ Uses centralized axios configuration
- ✅ No hardcoded URLs
- ✅ Automatic CORS and credential handling

### 3. **Image Assets** (`src/components/shared/ProductCard.jsx`)

**Before:**
```javascript
<img src={`${import.meta.env.VITE_BACK_END_URL}/${filePath}`} alt={filename} />
// Download link
window.open(`${import.meta.env.VITE_BACK_END_URL}/${filePath}`, "_blank")
```

**After:**
```javascript
<img src={`/${filePath}`} alt={filename} />
// Download link
window.open(`/${filePath}`, "_blank")
```

**Why:** Nginx serves files directly from backend through the reverse proxy, allowing relative paths to work seamlessly.

### 4. **Modal Preview** (`src/components/shared/ProductViewModal.jsx`)

**Before:**
```javascript
<img src={`${import.meta.env.VITE_BACK_END_URL}/${filePath}`} alt={filename} />
```

**After:**
```javascript
<img src={`/${filePath}`} alt={filename} />
```

### 5. **Dockerfile** (`Dockerfile`)

**Removed:**
```dockerfile
ARG VITE_BACK_END_URL=http://backend:8080
ENV VITE_BACK_END_URL=$VITE_BACK_END_URL
```

**Why:** Build-time environment variables are no longer needed since the frontend uses relative paths.

### 6. **Nginx Configuration** (`nginx.conf`)

**Added:**
```nginx
# Reverse proxy for API requests to backend service
location /api/ {
    proxy_pass http://backend:8080/api/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection 'upgrade';
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_cache_bypass $http_upgrade;
    proxy_redirect off;
}
```

**What it does:**
- ✅ Intercepts all requests to `/api/*`
- ✅ Forwards them to the backend service at `http://backend:8080/api/`
- ✅ Preserves client IP, protocol, and headers
- ✅ Enables WebSocket upgrades for future real-time features

### 7. **Docker Compose** (`docker-compose.yml`)

**Removed:**
```yaml
frontend:
  build:
    args:
      VITE_BACK_END_URL: ${VITE_BACK_END_URL:-http://localhost:8080}
  environment:
    VITE_BACK_END_URL: ${VITE_BACK_END_URL:-http://localhost:8080}
```

**Why:** No build-time or runtime configuration needed for the frontend anymore.

## How It Works

### Local Docker Development

```
Browser (localhost:3000)
    ↓
Nginx (port 3000)
    ├─ Static assets (JS, CSS, etc.) → /usr/share/nginx/html
    └─ /api/* requests → proxy_pass to backend:8080
    ↓
Backend (backend:8080/api)
    ↓
PostgreSQL (postgres:5432)
```

### EC2 Deployment

```
Browser (13.60.86.187:3000)
    ↓
Nginx (port 3000)
    ├─ Static assets → /usr/share/nginx/html
    └─ /api/* requests → proxy_pass to backend:8080 (internal Docker network)
    ↓
Backend Container (backend:8080/api)
    ↓
PostgreSQL (postgres:5432)
```

### Production Behind Load Balancer

```
User Browser
    ↓
Load Balancer (ALB/NLB)
    ├─ Port 80/443 → Frontend Nginx (EC2 instance)
    └─ Port 8080 → Backend (EC2 instance)
    ↓
Frontend Nginx intercepts /api/ and proxies to Backend
```

## API Flow Example

### Login Request

1. **Browser:** `POST /api/auth/signin` with credentials
2. **Nginx:** Intercepts request, forwards to `http://backend:8080/api/auth/signin`
3. **Backend:** Processes request, returns JWT token
4. **Nginx:** Forwards response back to browser
5. **Frontend:** Stores token in localStorage via interceptor

### File Upload Request

1. **Browser:** `POST /api/asset/add/file` with FormData
2. **Nginx:** Proxies to `http://backend:8080/api/asset/add/file`
3. **Backend:** Stores file, returns upload response with filePath
4. **Frontend:** Stores filePath in Redux state

### Image Display Request

1. **Browser:** `GET /{filePath}` (e.g., `/uploads/image.jpg`)
2. **Nginx:** Proxies to `http://backend:8080/{filePath}`
3. **Backend:** Returns image file from storage
4. **Browser:** Displays image

## Benefits

| Aspect | Before | After |
|--------|--------|-------|
| **Environment-specific config** | Frontend build args needed | None needed |
| **Deployment flexibility** | Required URL changes per env | Works everywhere |
| **Docker networking** | Frontend knew backend location | Transparent to frontend |
| **File serving** | Direct backend requests | Nginx-managed caching |
| **CORS handling** | Backend had to allow all origins | Nginx on same origin (no CORS) |
| **Caching** | No intermediary | Nginx can cache API responses |

## Testing

### Local Development

```bash
# Start with Docker Compose
docker compose up -d

# Test API calls
curl http://localhost:3000/api/public/asset

# Test file upload
curl -X POST http://localhost:3000/api/asset/add/file \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@document.pdf"

# Test file download
curl http://localhost:3000/uploads/document.pdf
```

### EC2 Deployment

```bash
# Assuming frontend is on 13.60.86.187:3000
# API requests automatically route through Nginx to backend

curl http://13.60.86.187:3000/api/public/asset
# Nginx forwards to: http://backend:8080/api/public/asset
# Which is the backend container on the same Docker network
```

## Environment Variables Reference

The following environment variables are **NO LONGER NEEDED** for the frontend:

- ❌ `VITE_BACK_END_URL` (deprecated)
- ❌ `REACT_APP_API_URL` (if existed)

The following backend variables are still required:

- ✅ `APP_CORS_ALLOWED_ORIGINS` (still needed for backend security)
- ✅ `SPRING_DATASOURCE_URL`
- ✅ `SPRING_DATASOURCE_USERNAME`
- ✅ `SPRING_DATASOURCE_PASSWORD`

## Migration Checklist

- [x] Updated `src/api/api.js` to use `/api` baseURL
- [x] Updated `src/pages/AddAsset.jsx` to use centralized api instance
- [x] Updated `src/components/shared/ProductCard.jsx` for relative paths
- [x] Updated `src/components/shared/ProductViewModal.jsx` for relative paths
- [x] Removed `VITE_BACK_END_URL` from `Dockerfile`
- [x] Added reverse proxy to `nginx.conf`
- [x] Removed env args from `docker-compose.yml`
- [x] Created `.env.example` for reference

## Next Steps

### For Local Development

```bash
# Rebuild frontend image
docker compose down frontend
docker compose build frontend
docker compose up frontend

# Verify logs show Nginx running on port 80
docker logs dams_frontend
```

### For EC2 Deployment

1. Pull latest code
2. Rebuild frontend image: `docker compose --env-file .env.prod build`
3. Restart services: `docker compose --env-file .env.prod down && docker compose --env-file .env.prod up -d`
4. Verify Nginx proxy is working:
   ```bash
   curl http://FRONTEND_IP:3000/api/public/asset
   ```

## Troubleshooting

### "Cannot GET /api/endpoint"

**Cause:** Nginx not properly routing to backend  
**Solution:**
```bash
# Check Nginx config is loaded
docker exec dams_frontend nginx -t

# Check backend is reachable from Nginx container
docker exec dams_frontend curl http://backend:8080/api/public/asset
```

### CORS errors still appearing

**Cause:** Backend CORS not configured correctly  
**Solution:** Verify `APP_CORS_ALLOWED_ORIGINS` in backend `.env` includes frontend origin

### Images not loading

**Cause:** Incorrect file paths in database  
**Solution:** Ensure backend returns filePath starting with `/uploads/` in API responses

### 502 Bad Gateway

**Cause:** Backend container not running or unreachable  
**Solution:**
```bash
docker compose ps  # Check all services are running
docker logs dams_backend  # Check for errors
```

## Questions?

Refer to [DOCKER_NETWORKING.md](../DOCKER_NETWORKING.md) for deep dive into Docker networking architecture.
