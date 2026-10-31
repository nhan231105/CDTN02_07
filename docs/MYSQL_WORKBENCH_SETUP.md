# Thiết lập MySQL Workbench và kết quả nghiệm thu local

**Trạng thái cập nhật 31/10/2026:** MySQL đã được tạo và chạy thành công trên máy local (9 bảng, 5 tài khoản demo, 24 tin seed). Spring Boot profile `mysql` kết nối HikariCP/JPA, đã nghiệm thu đọc/ghi tin tuyển dụng và dữ liệu sau restart. Phần hướng dẫn dưới đây còn giữ để tái thiết lập trên máy khác; lưu ý bộ kiểm thử tự động 101 API/6 Edge/29 backend có evidence chủ yếu từ `dev-h2`, **không tự suy ra toàn bộ PASS trên MySQL**.

## 1. Chuẩn bị server và backup

1. Mở MySQL Installer/Windows Services và xác nhận MySQL Server8 đang chạy. Workbench là client quản lý, không thay thế server.
2. Mở Workbench → dấu **+** cạnh **MySQL Connections**.
3. Connection Name: `Freelance local dev`; Hostname `127.0.0.1`; Port `3306`; Username là tài khoản quản trị local của bạn.
4. Bấm **Test Connection**, tự nhập mật khẩu trên máy; không gửi mật khẩu cho agent và không đưa vào Git.
5. Nếu database cũ có dữ liệu: **Server → Data Export**, chọn schema, **Export to Self-Contained File**, **Start Export**. Sao lưu cả thư mục file upload; DB metadata và CV/ảnh phải khôi phục cùng nhau.

## 2. Tạo schema và user ứng dụng

1. Chọn connection → **File → Open SQL Script**, mở `db/mysql/schema.sql`.
2. Đọc tên database trước khi chạy. Script dành cho schema dev mới `freelance_marketplace`, UTF8MB4, không chứa DROP/TRUNCATE. `IF NOT EXISTS` không tự nâng cấp schema cũ; không dùng nó để che sai lệch schema.
3. Bấm biểu tượng tia sét để chạy; **Refresh All** ở SCHEMAS, mở Tables. Có9 bảng gồm `stored_files`.
4. Mở tab SQL mới, tự thay mật khẩu placeholder rồi chạy:

```sql
CREATE USER 'freelance_app'@'localhost' IDENTIFIED BY '<tu-dat-mat-khau-local>';
GRANT SELECT, INSERT, UPDATE, DELETE
ON freelance_marketplace.* TO 'freelance_app'@'localhost';
```

User ứng dụng không cần quyền CREATE/DROP vì profile MySQL dùng JPA `validate`. DDL chạy bằng tài khoản quản trị có kiểm soát. Không ghi mật khẩu thật trở lại file SQL.

## 3. Dữ liệu demo tùy chọn

Trên **schema local mới**, có thể mở/chạy `db/mysql/seed_demo.sql`. Script tạo5 tài khoản demo BCrypt và24 tin; không chứa CV giả trỏ file không tồn tại. Upload CV/ảnh qua giao diện/API sau khi login.

Mật khẩu demo `DemoPass123!` chỉ dùng local. Không chạy seed demo ở production. Seed MySQL này không thay thế seed H2. Phần tích hợp MySQL local đã được kiểm chứng riêng bằng ca thử đọc/ghi và restart (xem `TEST_REPORT.md`).

## 4. Chạy profile MySQL có chủ đích

Mở PowerShell mới tại root repository; đặt biến môi trường cho **phiên terminal này**, không commit `.env` chứa secret:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:DB_URL = 'jdbc:mysql://localhost:3306/freelance_marketplace?useSSL=false&serverTimezone=UTC'
$env:DB_USERNAME = 'freelance_app'
# Nhập DB_PASSWORD vào biến môi trường của terminal từ prompt bảo mật (không paste literal vào script lưu trên Git).
$secure = Read-Host 'Mat khau MySQL' -AsSecureString
$cred = [pscredential]::new('freelance_app', $secure)
$env:DB_PASSWORD = $cred.GetNetworkCredential().Password
Remove-Variable secure, cred
# Truyền datasource trực tiếp theo cấu hình đã chạy thành công
$env:SPRING_DATASOURCE_URL = $env:DB_URL
$env:SPRING_DATASOURCE_USERNAME = $env:DB_USERNAME
$env:SPRING_DATASOURCE_PASSWORD = $env:DB_PASSWORD
$bytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:UPLOAD_DIRECTORY = 'D:/freelance-private-uploads'
.\mvnw.bat spring-boot:run
```

Chọn thư mục upload riêng, không nằm trong `public` hoặc static hosting. Đọc log: active profile phải là `mysql`, Hibernate validate thành công, server8082. Nếu thiếu bảng/cột hoặc access denied, sửa đúng schema/quyền; không đổi sang `ddl-auto=update/create-drop` để bỏ qua lỗi.

Frontend vẫn dùng `/api` qua Vite proxy8082; terminal frontend chạy `npm run dev` như README.

## 5. Xác minh và kiểm thử lại

```sql
SELECT VERSION(), DATABASE();
SHOW TABLES;
SELECT role, status, COUNT(*) FROM users GROUP BY role, status;
SELECT status, COUNT(*) FROM jobs GROUP BY status;
SELECT COUNT(*) FROM applications;
SELECT COUNT(*) FROM stored_files;
```

Sau đó chạy lại Maven tests theo cấu hình test H2 (không đổi chúng thành test MySQL ngoài ý muốn), API smoke và Playwright trên runtime MySQL:

```powershell
python -B scripts/api_smoke.py --report docs/evidence/mysql-api-smoke.json
cd freelance-frontend
npm run test:e2e
```

API smoke tạo account/job QA mới và yêu cầu tài khoản demo B/admin từ seed. Ghi rõ profile/runtime MySQL trong report khi thực sự chạy; script mặc định mô tả dev-h2 nên phải bổ sung thông tin môi trường cho lần MySQL. Kiểm tra cùng luồng7UC, UNIQUE/FK, khóa cạnh tranh, sort/pagination, DECIMAL, ENUM, BOOLEAN và Unicode tiếng Việt.

## 6. Quay lại H2 / khôi phục

- Dừng backend MySQL bằng Ctrl+C. Mở terminal mới không có các biến trên, hoặc xóa các biến task bằng `Remove-Item Env:SPRING_PROFILES_ACTIVE,Env:DB_URL,Env:DB_USERNAME,Env:DB_PASSWORD,Env:JWT_SECRET,Env:UPLOAD_DIRECTORY -ErrorAction SilentlyContinue`.
- Chạy lại backend mặc định `dev-h2`. Không xóa database MySQL để quay lại H2.
- Nếu cần khôi phục MySQL, dừng ứng dụng trước, dùng **Server → Data Import** vào schema phục hồi được đặt tên rõ, khôi phục file uploads tương ứng rồi kiểm tra toàn vẹn trước khi chuyển runtime.
- Chỉ xóa schema demo khi chủ dữ liệu xác nhận; không thực hiện thao tác đó trong đợt triển khai này.

## 7. Minh chứng cập nhật phiên local 31/10/2026

- `CREATE DATABASE`/9 bảng và seed bằng Workbench: thành công. Tổng 5 users (2 STUDENT/2 EMPLOYER/1 ADMIN), 24 jobs ban đầu (16 OPEN/4 FILLED/4 COMPLETED).
- `freelance_app` chỉ có SELECT, INSERT, UPDATE, DELETE trên `freelance_marketplace`; CLI và chương trình JDBC Java21 kết nối/SELECT thành công.
- Profile Spring Boot `mysql`, HikariCP và Tomcat8082: khởi động thành công, xác thực bằng log console.
- Tạo việc ID25 trên UI, xác nhận SQL có 25 jobs; sau restart backend API vẫn trả job ID25 và `totalElements=17` (chỉ OPEN). Trang chi tiết hiển thị tiếng Việt đúng dấu.

Nếu kiểm thử lại trên máy khác, kết quả phụ thuộc MySQL Server, credential và dữ liệu tại máy đó. Không commit `.env` chứa mật khẩu, không đưa bản dump dữ liệu người dùng thực lên GitHub.
