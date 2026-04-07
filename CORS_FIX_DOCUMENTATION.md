# EC2 CORS 403 Forbidden Error - Solution Documentation

## Problem Statement

When deploying the full-stack application (Spring Boot backend + React frontend with Nginx) to AWS EC2, all authentication requests (`/api/auth/signin`, `/api/auth/signup`) were returning **HTTP 403 Forbidden** with no CORS headers:

```
POST http://13.49.21.165:3001/api/auth/signin
Status: 403 Forbidden
Response: "Invalid CORS request"
Missing: Access-Control-Allow-Origin header
```

The same code worked perfectly on local development (`localhost:3001`).

---

## Root Cause Analysis

The issue had two layers:

### Layer 1: Missing `.env` File on EC2
- Spring Boot backend reads CORS origins from the `APP_CORS_ALLOWED_ORIGINS` environment variable
- This variable is set in `docker-compose.yml` from the `.env` file:
  ```yaml
  APP_CORS_ALLOWED_ORIGINS: ${APP_CORS_ALLOWED_ORIGINS:-http://localhost:3001,...}
  ```
- **The `.env` file didn't exist on the EC2 instance**, so Docker used the default fallback value which only included `localhost:3001`, not `13.49.21.165:3001`
- Result: Backend rejected requests from EC2's actual IP address

### Layer 2: Docker Using Registry Images Instead of Reading `.env`
- After creating the `.env` file, the docker-compose still wasn't passing the `APP_CORS_ALLOWED_ORIGINS` variable to containers
- Investigation revealed: The backend was using the **pre-built image from Docker registry** (`kaustubhgawade/backend:latest`)
- When docker-compose pulls a pre-built image, it doesn't rebuild the application, so environment variable substitution in `docker-compose.yml` isn't applied
- The `.env` file was being read by docker-compose for variable expansion, but the running container didn't receive the expanded value because it was a pulled image

Backend container environment (incorrect):
```
SPRING_JPA_HIBERNATE_DDL_AUTO=update
STRIPE_SECRET_KEY=sk_test_your_stripe_key_here
JAVA_TOOL_OPTIONS=-Xmx300m -Xss512k -XX:MaxMetaspaceSize=100m
(APP_CORS_ALLOWED_ORIGINS was missing!)
```

---

## Solution

### Step 1: Create `.env` File on EC2

The `.env` file must exist on the EC2 instance at `~/CC_PROJECT/.env`:

```bash
cat > ~/CC_PROJECT/.env << 'EOF'
# Database Configuration
DB_USER=myuser
DB_PASSWORD=mypassword
DB_NAME=mydb

# Backend Configuration
HIBERNATE_DDL=update
STRIPE_SECRET_KEY=sk_test_your_stripe_key_here

# CORS Configuration (for EC2 - use actual EC2 IP)
APP_CORS_ALLOWED_ORIGINS=http://13.49.21.165:3001,http://13.49.21.165,http://13.49.21.165:8080

# Frontend Configuration
VITE_BACK_END_URL=http://13.49.21.165:8080
EOF
```

**Key difference from local `.env`:**
- Local uses: `http://localhost:3001`
- EC2 uses: `http://13.49.21.165:3001` (the actual EC2 public IP)

### Step 2: Use Separate Docker-Compose for Deployment

For EC2 deployments, use a separate `docker-compose.prod.yml` that **explicitly passes environment variables from `.env` to running containers**:

```yaml
# docker-compose.prod.yml
services:
  backend:
    image: kaustubhgawade/backend:latest  # Uses registry image
    environment:
      APP_CORS_ALLOWED_ORIGINS: ${APP_CORS_ALLOWED_ORIGINS}  # Read from .env
      # ... other vars
```

**Critical difference:** The environment variables are assigned at **container runtime**, not at **build time**. Even though we're using a pre-built image, docker-compose still substitutes the `.env` values when starting the container.

### Step 3: Deploy

```bash
cd ~/CC_PROJECT

# Use production compose file
docker compose -f docker-compose.prod.yml up -d

# Verify CORS is set correctly in running container
docker exec dams_backend env | grep APP_CORS
# Output: APP_CORS_ALLOWED_ORIGINS=http://13.49.21.165:3001,http://13.49.21.165,http://13.49.21.165:8080
```

---

## Why This Works

1. **`.env` file exists** → docker-compose can read it
2. **docker-compose.prod.yml references `${APP_CORS_ALLOWED_ORIGINS}`** → Variable is substituted before container starts
3. **Backend container receives the correct CORS origins** → Spring Security allows requests from `http://13.49.21.165:3001`
4. **No 403 errors** → Authentication endpoints work correctly

---

## Testing

Before and after deployment:

### Before (403 Forbidden):
```bash
curl -X POST http://localhost:8081/api/auth/signin \
  -H "Origin: http://13.49.21.165:3001" \
  -d '{"username":"admin","password":"adminPass"}' \
  -i

# HTTP/1.1 403
# Invalid CORS request
```

### After (200 OK):
```bash
curl -X POST http://localhost:8081/api/auth/signin \
  -H "Origin: http://13.49.21.165:3001" \
  -d '{"username":"admin","password":"adminPass"}' \
  -i

# HTTP/1.1 200
# {"id":1,"username":"admin","jwtToken":"eyJhbGc..."}
```

---

## Key Learnings

| Issue | Solution |
|-------|----------|
| `.env` file doesn't exist on EC2 | Create it with EC2-specific values (IP instead of localhost) |
| Pre-built images don't inherit build-time vars | Use production docker-compose that passes env vars at runtime |
| Backend uses default CORS origins | Verify `docker exec dams_backend env \| grep APP_CORS` shows correct values |
| Can't trace environment variable issues | Use `docker inspect` and `docker exec env` commands to debug |

---

## Files Created

- **`docker-compose.prod.yml`** - Deployment configuration that uses registry images and properly passes `.env` variables
- **`.env`** - Local development (uses localhost)
- **`.env.prod.example`** - Template showing EC2 configuration format

## Deployment Checklist

- [ ] Update `.env` with EC2 IP address in `APP_CORS_ALLOWED_ORIGINS`
- [ ] Copy `docker-compose.prod.yml` to EC2
- [ ] Run `docker compose -f docker-compose.prod.yml up -d`
- [ ] Verify: `docker exec dams_backend env | grep APP_CORS`
- [ ] Test: `curl -X POST http://localhost:8081/api/auth/signin -H "Origin: http://<EC2-IP>:3001" ...`
- [ ] Test from browser: `http://<EC2-IP>:3001/login`
