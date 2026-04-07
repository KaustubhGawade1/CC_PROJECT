# Before & After: Code Examples

## 1. API Service Configuration

### Before: `src/api/api.js`
```javascript
import axios from "axios";

const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
    withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

❌ **Issues:**
- Depends on environment variable `VITE_BACK_END_URL`
- Must be set during Docker build
- Different values needed for local vs EC2
- Frontend coupled to backend location

### After: `src/api/api.js`
```javascript
import axios from "axios";

const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

✅ **Benefits:**
- No environment variables needed
- Works everywhere
- All requests proxied through Nginx
- Frontend completely decoupled

---

## 2. File Upload Implementation

### Before: `src/pages/AddAsset.jsx`
```javascript
import React, { useState } from "react";
import axios from "axios";
import { FaCloudUploadAlt } from "react-icons/fa";

const AddAsset = () => {
  // ... state setup ...
  
  const handleUpload = async (e) => {
    e.preventDefault();

    if (!file) {
      setMessage("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploading(true);
      setMessage("");

      // ❌ Direct axios call - hardcoded URL, manual auth header
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

      console.log("Upload response:", response.data);
      setMessage("✅ Asset uploaded successfully!");
      setFile(null);
    } catch (error) {
      console.error("Upload error:", error);
      setMessage("❌ Failed to upload asset.");
    } finally {
      setUploading(false);
    }
  };

  // ... rest of component ...
};
```

### After: `src/pages/AddAsset.jsx`
```javascript
import React, { useState } from "react";
import api from "../api/api";
import { FaCloudUploadAlt } from "react-icons/fa";

const AddAsset = () => {
  // ... state setup ...
  
  const handleUpload = async (e) => {
    e.preventDefault();

    if (!file) {
      setMessage("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploading(true);
      setMessage("");

      // ✅ Uses centralized api instance - relative path, auto auth header
      const response = await api.post(
        `/asset/add/file`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      console.log("Upload response:", response.data);
      setMessage("✅ Asset uploaded successfully!");
      setFile(null);
    } catch (error) {
      console.error("Upload error:", error);
      setMessage("❌ Failed to upload asset.");
    } finally {
      setUploading(false);
    }
  };

  // ... rest of component ...
};
```

**Changes:**
- Line 2: Import `api` instead of `axios`
- Line 38: Use `api.post()` instead of `axios.post()`
- Line 40: Relative path `/asset/add/file` instead of full URL
- Lines 43-44: Removed manual auth header (now auto-injected by interceptor)

---

## 3. Image Display in Cards

### Before: `src/components/shared/ProductCard.jsx`
```javascript
import { useState } from "react";
import { FaEye, FaTrashAlt, FaDownload } from "react-icons/fa";
import ProductViewModal from "./ProductViewModal";
import api from "../../api/api";

const ProductCard = ({ asset_id, filename, contentType, size, filePath, uploadedAt, uploadedBy, onDelete }) => {
  // ... state setup ...

  return (
    <div className="border rounded-lg shadow-xl...">
      <div className="w-full overflow-hidden aspect-[3/2]...">
        {safeContentType.includes("image") ? (
          // ❌ Hardcoded environment variable
          <img
            src={`${import.meta.env.VITE_BACK_END_URL}/${filePath}`}
            alt={filename}
          />
        ) : (
          <div className="text-gray-600 text-center">
            <FaEye size={48} />
            <p>Preview</p>
          </div>
        )}
      </div>

      <div className="p-4">
        {/* ... filename, type, size display ... */}

        <div className="flex flex-col gap-2 mt-4">
          <button
            // ❌ Hardcoded environment variable in download link
            onClick={() =>
              window.open(`${import.meta.env.VITE_BACK_END_URL}/${filePath}`, "_blank")
            }
            className="flex items-center justify-center gap-2 bg-gradient-to-r from-blue-500 to-purple-500..."
          >
            <FaDownload /> View / Download
          </button>

          <button
            onClick={handleDeleteAsset}
            className="flex items-center justify-center gap-2 bg-red-500..."
          >
            <FaTrashAlt /> Delete
          </button>
        </div>
      </div>
    </div>
  );
};
```

### After: `src/components/shared/ProductCard.jsx`
```javascript
import { useState } from "react";
import { FaEye, FaTrashAlt, FaDownload } from "react-icons/fa";
import ProductViewModal from "./ProductViewModal";
import api from "../../api/api";

const ProductCard = ({ asset_id, filename, contentType, size, filePath, uploadedAt, uploadedBy, onDelete }) => {
  // ... state setup ...

  return (
    <div className="border rounded-lg shadow-xl...">
      <div className="w-full overflow-hidden aspect-[3/2]...">
        {safeContentType.includes("image") ? (
          // ✅ Relative path
          <img
            src={`/${filePath}`}
            alt={filename}
          />
        ) : (
          <div className="text-gray-600 text-center">
            <FaEye size={48} />
            <p>Preview</p>
          </div>
        )}
      </div>

      <div className="p-4">
        {/* ... filename, type, size display ... */}

        <div className="flex flex-col gap-2 mt-4">
          <button
            // ✅ Relative path - works through Nginx proxy
            onClick={() =>
              window.open(`/${filePath}`, "_blank")
            }
            className="flex items-center justify-center gap-2 bg-gradient-to-r from-blue-500 to-purple-500..."
          >
            <FaDownload /> View / Download
          </button>

          <button
            onClick={handleDeleteAsset}
            className="flex items-center justify-center gap-2 bg-red-500..."
          >
            <FaTrashAlt /> Delete
          </button>
        </div>
      </div>
    </div>
  );
};
```

**Changes:**
- Line 17: `${import.meta.env.VITE_BACK_END_URL}/${filePath}` → `/${filePath}`
- Line 32: `${import.meta.env.VITE_BACK_END_URL}/${filePath}` → `/${filePath}`

---

## 4. Modal Image Preview

### Before: `src/components/shared/ProductViewModal.jsx`
```javascript
{contentType.includes("image") && filePath && (
  <div className="flex justify-center aspect-[3/2]">
    <img
      // ❌ Hardcoded environment variable
      src={`${import.meta.env.VITE_BACK_END_URL}/${filePath}`}
      alt={filename}
      className="object-cover"
    />
  </div>
)}
```

### After: `src/components/shared/ProductViewModal.jsx`
```javascript
{contentType.includes("image") && filePath && (
  <div className="flex justify-center aspect-[3/2]">
    <img
      // ✅ Relative path
      src={`/${filePath}`}
      alt={filename}
      className="object-cover"
    />
  </div>
)}
```

---

## 5. Docker Configuration

### Before: `Dockerfile`
```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# ❌ Build argument for environment variable
ARG VITE_BACK_END_URL=http://backend:8080
ENV VITE_BACK_END_URL=$VITE_BACK_END_URL

RUN npm run build

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

### After: `Dockerfile`
```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# ✅ No build arguments needed

RUN npm run build

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

---

## 6. Nginx Configuration

### Before: `nginx.conf`
```nginx
server {
    listen 80;
    server_name _;
    client_max_body_size 20M;

    root /usr/share/nginx/html;
    index index.html index.htm;

    # Cache control
    location ~* \.(js|css|png|jpg|...)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # ❌ No API reverse proxy - frontend makes direct backend calls
    
    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~ /\. {
        deny all;
    }
}
```

### After: `nginx.conf`
```nginx
server {
    listen 80;
    server_name _;
    client_max_body_size 20M;

    root /usr/share/nginx/html;
    index index.html index.htm;

    # Cache control
    location ~* \.(js|css|png|jpg|...)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # ✅ Reverse proxy for API requests
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

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~ /\. {
        deny all;
    }
}
```

---

## 7. Docker Compose

### Before: `docker-compose.yml`
```yaml
frontend:
  container_name: dams_frontend
  build:
    context: ./front-dams-main
    dockerfile: Dockerfile
    args:
      # ❌ Build argument for environment variable
      VITE_BACK_END_URL: ${VITE_BACK_END_URL:-http://localhost:8080}
  ports:
    - "3000:80"
  environment:
    # ❌ Runtime environment variable (not used, but confusing)
    VITE_BACK_END_URL: ${VITE_BACK_END_URL:-http://localhost:8080}
  depends_on:
    - backend
  networks:
    - dams_network
  restart: unless-stopped
```

### After: `docker-compose.yml`
```yaml
frontend:
  container_name: dams_frontend
  build:
    context: ./front-dams-main
    dockerfile: Dockerfile
  ports:
    - "3000:80"
  # ✅ No environment variables needed
  depends_on:
    - backend
  networks:
    - dams_network
  restart: unless-stopped
```

---

## Summary of Changes

| Component | Old Pattern | New Pattern | Files Changed |
|-----------|------------|------------|----------------|
| API Service | `${VITE_BACK_END_URL}/api` | `/api` | 1 |
| File Upload | `axios.post()` + hardcoded URL | `api.post()` + relative path | 1 |
| Image Display | `${VITE_BACK_END_URL}/${filePath}` | `/${filePath}` | 2 |
| Dockerfile | Build argument for URL | No arguments | 1 |
| Nginx | Static serving only | Static + reverse proxy | 1 |
| Docker Compose | Env variables for frontend | No frontend env vars | 1 |

**Total Files Modified: 7**
**Total API Hardcoding Instances Removed: 6**
**Build Arguments Removed: 1**
**Nginx Proxy Configurations Added: 1**
