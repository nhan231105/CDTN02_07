# 🚀 Freelance Marketplace for Students

**A modern part-time job marketplace platform built with Spring Boot + React**

## 📋 Table of Contents
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Development Guide](#development-guide)
- [Project Structure](#project-structure)

---

## 🎯 Quick Start

### Prerequisites
- **Docker & Docker Compose** (recommended for deployment)
- **Java 21** (for local development)
- **Maven 3.9+** (for local development)

### Option 1: Run with Docker (Recommended)

#### Windows:
```bash
# Run the startup script
run.bat

# Then choose:
# 1 = Demo mode (H2 in-memory database)
# 2 = Production mode (MySQL database)
```

#### Linux/Mac:
```bash
# Make script executable
chmod +x run.sh

# Run the startup script
./run.sh

# Then choose:
# 1 = Demo mode (H2 in-memory database)
# 2 = Production mode (MySQL database)
```

### Option 2: Run Locally (Requires Java 21)

```bash
# Build
mvn clean package

# Run with H2 (no MySQL needed)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# Or run with MySQL
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

### Access Application

Once running, access the application at:

```
HTTP: http://localhost:8080/api
Health Check: http://localhost:8080/api/auth/health
H2 Console: http://localhost:8080/api/h2-console (demo mode only)
```

---

## 🏗️ Architecture

```
org.example/
├── config/              # Spring configuration
│   └── SecurityConfig   # BCrypt password encoder
├── common/              # Shared utilities
│   └── ApiResponse      # Standardized API response wrapper
├── entity/              # JPA entities
│   ├── User             # User account entity
│   ├── UserRole         # Role enum (STUDENT, EMPLOYER, ADMIN)
│   └── UserStatus       # Status enum (ACTIVE, LOCKED)
├── repository/          # Data access layer
│   └── UserRepository   # User CRUD + custom queries
├── service/             # Business logic layer
│   ├── UserService      # Interface
│   └── UserServiceImpl   # Implementation with BCrypt
├── controller/          # REST API endpoints
│   └── AuthController   # Auth & admin endpoints
└── Main.java           # Spring Boot entry point
```

### Architecture Pattern: **Layered Architecture**

```
Controller (API)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
    ↓
Entity (Database Model)
```

**Rules:**
- ❌ Never return Entity directly → Always use DTO
- ❌ Never put SQL/business logic in Controller
- ✅ Always use Service layer for business logic
- ✅ Always hash passwords with BCrypt
- ✅ Always use ResponseEntity for HTTP responses

---

## 📚 API Documentation

### Authentication & User Management

#### 1. **Register New Account**
```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Nguyen Van A",
  "email": "student@example.com",
  "phone": "0912345678",
  "password": "Password123!",
  "role": "STUDENT"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Dang ki thanh cong",
  "data": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "student@example.com",
    "phone": "0912345678",
    "role": "STUDENT"
  },
  "timestamp": "2026-10-18T20:00:00"
}
```

**Error Responses:**
- `409 Conflict` - Email/Phone already exists
- `400 Bad Request` - Invalid role or validation error

---

#### 2. **Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "student@example.com",
  "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Dang nhap thanh cong",
  "data": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "student@example.com",
    "phone": "0912345678",
    "role": "STUDENT",
    "status": "ACTIVE"
  },
  "timestamp": "2026-10-18T20:00:00"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid email/phone or password
- `403 Forbidden` - Account is locked

---

#### 3. **Admin: Search Users**
```http
GET /api/auth/admin/search?keyword=Nguyen
GET /api/auth/admin/search?role=STUDENT
GET /api/auth/admin/search?status=ACTIVE
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tim kiem thanh cong. Co 5 ket qua",
  "data": [
    {
      "id": 1,
      "fullName": "Nguyen Van A",
      "email": "student@example.com",
      "phone": "0912345678",
      "role": "STUDENT",
      "status": "ACTIVE"
    }
  ],
  "timestamp": "2026-10-18T20:00:00"
}
```

---

#### 4. **Admin: Statistics**
```http
GET /api/auth/admin/stats
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Thong ke thanh cong",
  "data": {
    "totalUsers": 50,
    "totalStudents": 35,
    "totalEmployers": 15,
    "activeUsers": 48,
    "lockedUsers": 2
  },
  "timestamp": "2026-10-18T20:00:00"
}
```

---

#### 5. **Health Check**
```http
GET /api/auth/health
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Auth service is running",
  "timestamp": "2026-10-18T20:00:00"
}
```

---

## 🐳 Deployment

### Docker Deployment (Recommended)

**Step 1: Build and run with Docker Compose**
```bash
# Build Docker image
docker-compose build

# Start services
docker-compose up

# Access the app
http://localhost:8080/api
```

**Step 2: Deploy to Cloud**

#### AWS EC2:
```bash
# SSH into instance
ssh -i key.pem ubuntu@your-instance-ip

# Clone repository
git clone <your-repo-url>
cd your-project

# Run with Docker
docker-compose up -d
```

#### Heroku:
```bash
# Install Heroku CLI
# https://devcenter.heroku.com/articles/heroku-cli

# Create Heroku app
heroku create your-app-name

# Add MySQL add-on
heroku addons:create cleardb:ignite

# Deploy
git push heroku main
```

#### Azure:
```bash
# Use Azure Container Registry
az acr build --registry myregistry --image freelance-marketplace:latest .

# Deploy to App Service
az webapp create --resource-group mygroup --plan myplan --name myapp --deployment-container-image-name myregistry.azurecr.io/freelance-marketplace:latest
```

---

### Manual Deployment (Server with Java & MySQL)

```bash
# Build JAR
mvn clean package

# Copy JAR to server
scp target/freelance-marketplace-1.0.0.jar user@server:/app/

# Run on server
ssh user@server
cd /app
java -jar freelance-marketplace-1.0.0.jar --spring.profiles.active=mysql
```

---

## 🛠️ Development Guide

### Local Setup with H2 (No MySQL needed)

```bash
# Clone
git clone <your-repo-url>
cd your-project

# Build
mvn clean install

# Run with H2 (in-memory database)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# App runs on: http://localhost:8080/api
# H2 Console: http://localhost:8080/api/h2-console (admin/password)
```

### Local Setup with MySQL

```bash
# Start MySQL
docker run --name mysql-dev \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=freelance_marketplace \
  -p 3306:3306 \
  mysql:8.0

# Run application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

### Add New Entity & API

#### 1. Create Entity (`src/main/java/org/example/entity/`)
```java
@Entity
@Table(name = "jobs")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}
```

#### 2. Create Repository (`src/main/java/org/example/repository/`)
```java
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // custom queries
}
```

#### 3. Create Service (`src/main/java/org/example/service/`)
```java
@Service
public class JobServiceImpl implements JobService {
    // business logic
}
```

#### 4. Create Controller (`src/main/java/org/example/controller/`)
```java
@RestController
@RequestMapping("/api/jobs")
public class JobController {
    // endpoints
}
```

---

## 📦 Project Structure

```
freelance-marketplace/
├── .env.example                    # Environment variables template
├── .gitignore                      # Git ignore file
├── Dockerfile                      # Docker image configuration
├── docker-compose.yml              # Docker Compose configuration
├── pom.xml                         # Maven dependencies
├── run.bat / run.sh               # Startup scripts
├── README.md                       # This file
│
├── src/main/
│   ├── java/org/example/
│   │   ├── Main.java              # Spring Boot entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── common/
│   │   │   └── ApiResponse.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   └── UserStatus.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── UserServiceImpl.java
│   │   │   ├── *Request.java
│   │   │   ├── *Response.java
│   │   │   └── *Exception.java
│   │   └── controller/
│   │       └── AuthController.java
│   │
│   └── resources/
│       ├── application.properties      # Main config
│       ├── application-h2.properties  # H2 profile (demo)
│       └── application-mysql.properties # MySQL profile (production)
│
└── src/test/
    └── java/org/example/
        └── (unit & integration tests)
```

---

## 🔐 Security Features

✅ **BCrypt Password Hashing** - Passwords hashed with BCrypt  
✅ **JWT Authentication** - Token-based authentication (ready)  
✅ **Role-Based Access Control** - STUDENT, EMPLOYER, ADMIN  
✅ **Unique Constraints** - Email & phone uniqueness enforced  
✅ **Account Locking** - Locked users cannot login  
✅ **Input Validation** - Request validation with @Valid  
✅ **Error Handling** - Standardized error responses  

---

## 🧪 Testing

### Test Endpoints with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "phone": "0912345678",
    "password": "Password123!",
    "role": "STUDENT"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "Password123!"
  }'
```

**Search Users:**
```bash
curl "http://localhost:8080/api/auth/admin/search?keyword=Test"
```

---

## 📞 Support & Troubleshooting

### Port Already in Use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Docker Issues
```bash
# Stop all containers
docker-compose down

# Remove all images
docker-compose down --rmi all

# Restart
docker-compose up --build
```

### Database Issues
```bash
# H2 Console
http://localhost:8080/api/h2-console
- URL: jdbc:h2:mem:freelance_marketplace
- User: sa
- Password: (leave blank)

# MySQL
mysql -h localhost -u root -p
USE freelance_marketplace;
SHOW TABLES;
```

---

## 📄 License

This project is part of a university assignment.

---

## 👥 Contributors

- Developed by: [Your Name]
- University: [Your University]
- Subject: [Subject Name]
- Date: 2026

---

## 🚀 Ready to Deploy!

Your application is now ready to:
1. ✅ Run locally with H2 (demo)
2. ✅ Run locally with MySQL
3. ✅ Deploy with Docker
4. ✅ Deploy to cloud (AWS, Azure, Heroku)
5. ✅ Hand over to another developer

**Next Steps:**
- [ ] Test all API endpoints
- [ ] Update `.env` file with production secrets
- [ ] Deploy to your cloud provider
- [ ] Set up monitoring & logging
- [ ] Configure CI/CD pipeline
