# Freelance Marketplace — 7 Use Case

React + Spring Boot trong repository hiện hữu. Phạm vi: đăng nhập, đăng ký, hồ sơ/CV/ảnh, tìm việc, ứng tuyển, đánh giá NTD, thông báo; có employer workflow để duyệt và hoàn thành công việc. Admin có đăng nhập/role gate và dashboard cơ bản; không tuyên bố hoàn tất FR-13…15.

## 🚀 Chạy nhanh với MySQL (cho người được bàn giao project)

Dùng schema sạch để chạy local hoặc tạo database demo riêng.

**1) Cài đặt yêu cầu:** Java21, Node22/24, MySQL Server8 + MySQL Workbench đã cài và chạy.

**2) Import database (1 lần):**
- Mở MySQL Workbench → kết nối MySQL local của bạn.
- Mở và chạy `db/mysql/schema.sql` trên MySQL local dành cho dự án. Với schema cũ, sao lưu và xem hướng dẫn migration bên dưới.
- DB mới sẽ trống. Đăng ký tài khoản qua giao diện sau khi khởi động.

**3) Trỏ backend vào MySQL của bạn** — mở PowerShell tại thư mục gốc:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/freelance_marketplace?useSSL=false&serverTimezone=UTC'
$env:DB_USERNAME = 'root'              # đổi theo user MySQL của bạn
# Set DB_PASSWORD in your shell or use the secret manager for your environment.
.\mvnw.bat spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

Thấy log `Tomcat started on port(s): 8082` và active profile `mysql` là backend đã chạy đúng (`http://localhost:8082/api`).

**4) Chạy frontend** — mở PowerShell thứ 2:

```powershell
cd freelance-frontend
npm ci
npm run dev
```

Mở trình duyệt `http://localhost:5173` và đăng ký tài khoản student hoặc employer.


## Yêu cầu

- Java21; Node `^20.19.0 || >=22.12.0` theo Vite, khuyến nghị Node22/24.
- Windows PowerShell; Maven wrapper có sẵn, lần đầu cần mạng để tải Maven/dependencies.
- Python3 để chạy REST smoke; Microsoft Edge cho Playwright trên máy Windows này.
- MySQL 8 dùng cho ứng dụng. Test Maven dùng H2 cô lập.

## Chạy local

Terminal1 ở root:

```powershell
.\mvnw.bat spring-boot:run
```

Backend chạy profile mặc định `mysql` tại `http://localhost:8082/api`. Endpoint kiểm tra là `/api/auth/health`.

Để chạy demo và kiểm thử tích hợp bằng artifact ổn định, build xong rồi khởi động JAR:

```powershell
.\mvnw.bat package
java -jar target/freelance-marketplace-1.0.0.jar --spring.profiles.active=mysql
```

JAR đã đóng gói tách phiên chạy khỏi thư mục class đang biên dịch. Khi dùng `spring-boot:run`, dừng phiên đó trước khi chạy một build khác cùng repository; Maven có thể thay class trong `target/classes` khi ứng dụng đang tải chúng.

Terminal2:

```powershell
cd freelance-frontend
npm ci
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Frontend: `http://127.0.0.1:5173`. Client gọi `/api` qua Vite proxy8082; triển khai khác origin đặt `VITE_API_BASE_URL`, backend đặt `CORS_ORIGINS` tương ứng. Dừng process của từng terminal bằng Ctrl+C. `--strictPort` báo lỗi khi cổng bận để tránh frontend tự chuyển sang cổng khác.

## Tài khoản demo — chỉ dev

Tạo dữ liệu demo bằng tài khoản tự đăng ký hoặc bật demo initializer trên DB local riêng. Cấu hình mật khẩu demo trong môi trường chạy local.


Có thể đăng ký tài khoản student/employer mới để thử luồng độc lập. Không cho tự đăng ký ADMIN. Đăng ký thành công chuyển login; JWT một giờ, logout xóa phiên client, token sai/hết hạn yêu cầu login lại.

## Kiểm thử

```powershell
.\mvnw.bat test
.\mvnw.bat package
python -B scripts/api_smoke.py
cd freelance-frontend
npm run lint
npm run build
npm test
npm run format:check
npm run test:e2e
```

API smoke và E2E cũ dùng dữ liệu demo fixture. Chỉ chạy chúng trên bản thử nghiệm riêng sau khi cấu hình fixture tương ứng, bao gồm `APP_DEMO_PASSWORD` trong terminal chạy test.


## Luồng demo

1. Student đăng ký/login → hồ sơ → lưu thông tin, upload CV PDF và ảnh.
2. Tìm/lọc việc, xem chi tiết, nộp đơn có CV và consent; xem lịch sử đơn.
3. Employer đăng nhập → tin của mình → ứng viên → duyệt/từ chối.
4. Student nhận notification, đánh dấu đã đọc; employer chuyển OPEN→FILLED→COMPLETED.
5. Student đã APPROVED đánh giá NTD một lần, tải lại để kiểm tra dữ liệu.

Chỉ owner và employer của đơn được tải CV. Thay CV hiện tại không làm mất CV đã nộp. Đổi email/SĐT/mật khẩu yêu cầu mật khẩu hiện tại và giới hạn48h; đổi mật khẩu yêu cầu login lại.

## Cấu hình và tài liệu

- `mysql` là profile mặc định. File dùng `UPLOAD_DIRECTORY`, mặc định là `uploads/`. H2 được cấu hình cho kiểm thử.
- `mysql`: schema validate, credentials qua `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `JWT_SECRET` tối thiểu32byte. Không tự chuyển sang MySQL.
- `.env.example` là mẫu tham khảo; Spring đọc biến môi trường, không tự nạp file `.env`. Xem hướng dẫn Workbench để đặt biến trong PowerShell.
- Khi dùng Docker Compose local, cấu hình `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD` và `JWT_SECRET` trong môi trường hoặc file `.env` riêng. Khi chạy trực tiếp ngoài profile `render` mà bỏ trống `JWT_SECRET`, backend sinh khóa tạm và phiên đăng nhập sẽ hết hiệu lực sau khi khởi động lại.
- [API Contract](docs/API_CONTRACT.md), [ERD](docs/ERD.md), [truy vết7UC](docs/REQUIREMENTS_TRACEABILITY.md), [quyết định/sửa lỗi](docs/DECISIONS_AND_FIXES.md).
- [Nguồn bảo toàn](docs/reference/INDEX.md), [cần xác nhận](docs/REVIEW_REQUIRED.md), [báo cáo triển khai](docs/IMPLEMENTATION_REPORT.md), [kế hoạch commit](docs/COMMIT_PLAN.md).
- [MySQL Workbench](docs/MYSQL_WORKBENCH_SETUP.md) có ghi chép cấu hình local. Dùng phần Deploy bên dưới cho Render.

## Deploy demo công khai trên Render

Xem [bàn giao trước merge](docs/CAMPUSWORK_HANDOFF.md) để cấu hình email, chạy migration 07–08, kiểm tra luồng nghiệp vụ và chuẩn bị Pull Request.

1. Tạo database MySQL 8 dành riêng cho demo và nạp [schema.sql](db/mysql/schema.sql). Schema mới đã có các cột Exam Mode. Với bản sao DB cũ, kiểm tra schema và chạy [migration 06](db/mysql/migration_06_exam_mode.sql) nếu thiếu cột. Sao lưu trước khi cập nhật DB đã có dữ liệu. Hibernate kiểm tra schema khi khởi động và ứng dụng không tự chạy migration. Nếu nhà cung cấp cấp sẵn tên database, chọn database đó và điều chỉnh các câu lệnh `CREATE DATABASE`, `USE` và `SHOW TABLES FROM` trong bản SQL dùng để import.
2. Tạo **Web Service**, chọn **Docker**, dùng root repo và `./Dockerfile`. Để trống Docker Command. Đặt Health Check Path là `/api/auth/health`. Spring đọc `PORT` do Render cấp. Giữ `/api` làm context path.
3. Đặt `SPRING_PROFILES_ACTIVE=mysql,render`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`, `UPLOAD_DIRECTORY`, `RESEND_API_KEY` và `CAMPUSWORK_EMAIL_FROM` trong Render Dashboard. Dùng JDBC URL với TLS theo nhà cung cấp MySQL. JWT cần khóa ngẫu nhiên riêng, tối thiểu 32 byte. Profile `render` bắt buộc cấu hình khóa và vô hiệu hóa demo initializer.
4. Gắn persistent disk vào Web Service tại `/var/data`, đặt `UPLOAD_DIRECTORY=/var/data/uploads`. Nếu chuyển dữ liệu có upload, sao chép các tệp tương ứng với metadata `stored_files` vào disk trước khi mở quyền truy cập. Thử tải lại một tệp sau một lần redeploy.
5. Tạo **Static Site**, Root Directory `freelance-frontend`, Build Command `npm ci --include=dev && npm run build`, Publish Directory `dist`. Đặt `NODE_VERSION` là bản Node 24 đã kiểm thử. Đặt `VITE_API_BASE_URL` bằng URL HTTPS công khai của backend kèm `/api`, bỏ dấu `/` cuối. Thêm Rewrite từ `/*` tới `/index.html`.
6. Đặt `CORS_ORIGINS` thành origin HTTPS chính xác của Static Site, bỏ path và dấu `/` cuối. Kiểm tra health, refresh route con, đăng ký, đăng nhập, quyền Admin và upload/download trước khi mở demo.
7. Kết nối hai service với repo GitHub của nhóm và chọn nhánh `main`. Bật Auto-Deploy **On Commit** hoặc **After CI Checks Pass** nếu repo phát CI checks. Trước khi công khai GitHub, kiểm tra cả file đã track và lịch sử Git. Giữ `.env`, dump dữ liệu, file upload và tài liệu có dữ liệu cá nhân ở ngoài repo công khai. Git ignore chỉ áp dụng với file chưa được track.

Demo công khai nên dùng tài khoản và dữ liệu tổng hợp. Để kiểm tra dashboard Admin, người quản trị có quyền DB cần cấp vai trò cho một tài khoản chuyên dùng đã xác minh qua quy trình nội bộ, rồi đăng nhập lại. API đăng ký không cấp quyền ADMIN. Khi chạy local, có thể bật demo initializer có chủ đích bằng `APP_DEMO_SEED` và cấu hình `APP_DEMO_PASSWORD` riêng cho DB dùng thử.
