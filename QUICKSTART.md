# Chạy nhanh — H2 trước MySQL

Hướng dẫn đầy đủ: [README](README.md). Trạng thái thực chạy: [TEST_REPORT](docs/TEST_REPORT.md).

Yêu cầu Java21, Node20.19 hoặc22.12+; mạng cho lần tải dependency đầu tiên.

## Terminal backend

```powershell
.\mvnw.bat spring-boot:run
```

Mặc định `dev-h2`, REST `http://localhost:8082/api`, health `/api/auth/health`. H2 seed dữ liệu demo; restart mất dữ liệu đã thay đổi và seed lại. Chưa cần MySQL.

Phiên demo/QA có thể dùng JAR ổn định sau khi build:

```powershell
.\mvnw.bat package
java -jar target/freelance-marketplace-1.0.0.jar --spring.profiles.active=dev-h2
```

Không chạy hai backend cùng cổng. Dừng `spring-boot:run` trước một build khác dùng chung `target/classes`; bản JAR giúp phiên chạy tách khỏi class đang biên dịch.

## Terminal frontend

```powershell
cd freelance-frontend
npm ci
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Mở `http://127.0.0.1:5173`. Đăng nhập `student.a@demo.local` hoặc `employer.a@demo.local`, mật khẩu dev `DemoPass123!`.

## Kiểm tra

```powershell
.\mvnw.bat test
python -B scripts/api_smoke.py
```

Smoke cần backend đang chạy; browser E2E cần cả frontend. Xem README cho lint/build/unit/E2E và docs cho contract/Workbench.

Nếu cổng bận, xác định process hoặc đổi cấu hình cổng và proxy tương ứng. Giữ nguyên process và database không thuộc phiên chạy dự án này.
