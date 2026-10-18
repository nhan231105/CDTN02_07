# ⚡ QUICKSTART - Freelance Marketplace

## 🚀 Chạy Demo ngay (30 giây)

### Bước 1: Cài Docker
- **Windows/Mac:** https://www.docker.com/products/docker-desktop
- **Linux:** `sudo apt-get install docker.io docker-compose`

### Bước 2: Run Project
**Windows:**
```bash
run.bat
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

### Bước 3: Chọn chế độ
```
1 = Demo mode (H2 - không cần MySQL)
2 = Production mode (MySQL)
```
Chọn **1** nếu lần đầu!

### Bước 4: Chờ app khởi động
```
✅ App started on port 8080
✅ Database ready
✅ API endpoints available
```

---

## 📌 API Endpoints

**Base URL:** `http://localhost:8080/api`

### Đăng ký
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van A",
    "email": "student@example.com",
    "phone": "0912345678",
    "password": "Password123!",
    "role": "STUDENT"
  }'
```

### Đăng nhập
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "student@example.com",
    "password": "Password123!"
  }'
```

### Admin: Tìm kiếm user
```bash
curl "http://localhost:8080/api/auth/admin/search?keyword=Nguyen"
curl "http://localhost:8080/api/auth/admin/search?role=STUDENT"
```

### Admin: Thống kê
```bash
curl http://localhost:8080/api/auth/admin/stats
```

### Health Check
```bash
curl http://localhost:8080/api/auth/health
```

---

## 🗄️ H2 Database Console (Demo mode)
```
URL: http://localhost:8080/api/h2-console
JDBC URL: jdbc:h2:mem:freelance_marketplace
User: sa
Password: (leave blank)
```

---

## 📂 File Structure
```
├── Dockerfile              # Docker image
├── docker-compose.yml      # Services config
├── run.bat / run.sh       # Startup scripts
├── .env.example           # Environment template
├── pom.xml                # Maven dependencies
├── README.md              # Full documentation
├── QUICKSTART.md          # This file
│
├── src/main/java/org/example/
│   ├── entity/            # Database models
│   ├── repository/        # Data access layer
│   ├── service/           # Business logic
│   ├── controller/        # API endpoints
│   └── config/            # Configuration
│
└── src/main/resources/
    ├── application.properties        # Main config
    ├── application-h2.properties    # Demo config
    └── application-mysql.properties # Production config
```

---

## 🛑 Stop Application
```bash
# Press Ctrl+C in terminal

# Or with Docker
docker-compose down
```

---

## 📦 Deploy lên Web

### Option 1: Docker Hub
```bash
# Build & push image
docker build -t your-username/freelance-marketplace .
docker push your-username/freelance-marketplace

# Deploy: Dùng AWS, Azure, Heroku, etc
```

### Option 2: Heroku (Free)
```bash
# Install Heroku CLI
# https://devcenter.heroku.com/articles/heroku-cli

# Create app
heroku create your-app-name

# Add MySQL
heroku addons:create cleardb:ignite

# Deploy
git push heroku main
```

### Option 3: AWS/Azure/DigitalOcean
- Copy Dockerfile + docker-compose.yml
- Deploy container
- Xem full documentation ở README.md

---

## ❓ Troubleshooting

**Port 8080 đang được sử dụng:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

**Docker không khởi động:**
```bash
docker-compose down --rmi all
docker-compose up --build
```

**Database lỗi:**
```bash
docker-compose down -v  # Remove volumes
docker-compose up --build
```

---

## ✅ Checklist trước bàn giao
- [ ] Test API endpoints
- [ ] Cập nhật `.env` file
- [ ] Cập nhật README.md với info của bạn
- [ ] Commit & push to Git
- [ ] Deploy to cloud
- [ ] Test on production

---

**Xong! Ứng dụng sẵn sàng bàn giao.** 🎉
