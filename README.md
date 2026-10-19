# Freelance Marketplace

Yêu cầu: JDK 21+, Node.js 18+. Không cần cài Maven (đã có mvnw).

## Backend (http://localhost:8082)
```
.\mvnw.bat spring-boot:run
```
Dùng H2 in-memory, không cần cài database (dữ liệu mất khi tắt).

## Frontend (http://localhost:5173)
```
cd freelance-frontend
npm install
npm run dev
```
