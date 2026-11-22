# Chạy nhanh với MySQL

Hướng dẫn đầy đủ: [README](README.md). Trạng thái thực chạy: [TEST_REPORT](docs/TEST_REPORT.md).

Yêu cầu Java21, Node20.19 hoặc22.12+; mạng cho lần tải dependency đầu tiên.

## Terminal backend

```powershell
.\mvnw.bat spring-boot:run
```

Mặc định profile `mysql`, REST `http://localhost:8082/api` và health `/api/auth/health`. Chuẩn bị database riêng và cấu hình kết nối theo README. Maven test dùng H2.

Phiên demo/QA có thể dùng JAR ổn định sau khi build:

```powershell
.\mvnw.bat package
java -jar target/freelance-marketplace-1.0.0.jar --spring.profiles.active=mysql
```

Không chạy hai backend cùng cổng. Dừng `spring-boot:run` trước một build khác dùng chung `target/classes`; bản JAR giúp phiên chạy tách khỏi class đang biên dịch.

## Terminal frontend

```powershell
cd freelance-frontend
npm ci
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Mở `http://127.0.0.1:5173` và đăng ký tài khoản thử.

## Kiểm tra

```powershell
.\mvnw.bat test
python -B scripts/api_smoke.py
```

Chỉ chạy smoke và E2E cũ với fixture phù hợp trên môi trường thử nghiệm riêng.

Nếu cổng bận, xác định process hoặc đổi cấu hình cổng và proxy tương ứng. Giữ nguyên process và database không thuộc phiên chạy dự án này.
