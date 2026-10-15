# 🐳 Hướng dẫn sử dụng Docker - Dishcision

---

### 1. Khởi động hệ thống

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

---

### 2. Dừng hệ thống
- **Dừng và xóa container**

  ```bash
  docker compose -f docker/docker-compose.yml down
  ```
- **Tạm dừng không xóa container**:
 
  ```bash
  docker compose -f docker/docker-compose.yml stop
  ```

- **Tắt và XÓA TOÀN BỘ dữ liệu**:
  
  ```bash
  docker compose -f docker/docker-compose.yml down -v
  ```

---

### 3. Chỉ chạy Database

```bash
docker compose -f docker/docker-compose.yml up -d postgres redis
```

```bash
cd backend
npm run dev
```

---

### 4. Chỉ chạy / Khởi động lại Backend
- Chạy riêng service backend:
  
  ```bash
  docker compose -f docker/docker-compose.yml up -d backend
  ```

- Khởi động lại container backend sau khi cập nhật code:
  
  ```bash
  docker restart dishcision-backend
  ```

---

### 5. Xem Log & Database
- **Xem log thời gian thực của backend**:
  
  ```bash
  docker logs -f dishcision-backend
  ```

- **Xem database trực quan**:
  
  ```bash
  cd backend
  npx prisma studio
  ```