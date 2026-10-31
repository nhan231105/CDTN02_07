# Freelance Marketplace — 7 Use Case

React + Spring Boot trong repository hiện hữu. Phạm vi: đăng nhập, đăng ký, hồ sơ/CV/ảnh, tìm việc, ứng tuyển, đánh giá NTD, thông báo; có employer workflow để duyệt và hoàn thành công việc. Admin có đăng nhập/role gate và dashboard cơ bản; không tuyên bố hoàn tất FR-13…15.

## Yêu cầu

- Java21; Node `^20.19.0 || >=22.12.0` theo Vite, khuyến nghị Node22/24.
- Windows PowerShell; Maven wrapper có sẵn, lần đầu cần mạng để tải Maven/dependencies.
- Python3 để chạy REST smoke; Microsoft Edge cho Playwright trên máy Windows này.
- Không cần MySQL/Docker để demo. Mặc định `dev-h2`, dữ liệu in-memory mất sau restart và được seed lại.

## Chạy local

Terminal1 ở root:

```powershell
.\mvnw.bat spring-boot:run
```

Backend: `http://localhost:8082/api`; health: `http://localhost:8082/api/auth/health`. Profile dev seed24 tin,5 account, hồ sơ/CV và các trạng thái mẫu. Không dùng account/secret dev ngoài local.

Để chạy demo và kiểm thử tích hợp bằng artifact ổn định, build xong rồi khởi động JAR:

```powershell
.\mvnw.bat package
java -jar target/freelance-marketplace-1.0.0.jar --spring.profiles.active=dev-h2
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

Tất cả dùng mật khẩu `DemoPass123!`:

| Vai trò | Email |
|---|---|
| Student A | `student.a@demo.local` |
| Student B | `student.b@demo.local` |
| Employer A | `employer.a@demo.local` |
| Employer B | `employer.b@demo.local` |
| Admin | `admin@demo.local` |

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

API smoke và E2E cần backend/frontend đang chạy, tạo dữ liệu QA riêng trong H2. Evidence API được ghi ở `docs/evidence/api-smoke.json`, không lưu token. Lệnh E2E dùng Edge đã cài; nếu máy khác chưa có Edge, cài browser Playwright tương ứng và cấu hình channel theo môi trường.

Kết quả thực chạy và giới hạn nằm trong [TEST_REPORT](docs/TEST_REPORT.md). Không suy ra PASS từ việc test mới được viết hoặc từ baseline “No tests to run”.

## Luồng demo

1. Student đăng ký/login → hồ sơ → lưu thông tin, upload CV PDF và ảnh.
2. Tìm/lọc việc, xem chi tiết, nộp đơn có CV và consent; xem lịch sử đơn.
3. Employer đăng nhập → tin của mình → ứng viên → duyệt/từ chối.
4. Student nhận notification, đánh dấu đã đọc; employer chuyển OPEN→FILLED→COMPLETED.
5. Student đã APPROVED đánh giá NTD một lần, tải lại để kiểm tra dữ liệu.

Chỉ owner và employer của đơn được tải CV. Thay CV hiện tại không làm mất CV đã nộp. Đổi email/SĐT/mật khẩu yêu cầu mật khẩu hiện tại và giới hạn48h; đổi mật khẩu yêu cầu login lại.

## Cấu hình và tài liệu

- `dev-h2`: mặc định, JPA create-drop chỉ dev; H2 console tắt. File riêng tại `UPLOAD_DIRECTORY` (mặc định thư mục `uploads/`, Git ignore), không public hóa thư mục này.
- `mysql`: schema validate, credentials qua `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `JWT_SECRET` tối thiểu32byte. Không tự chuyển sang MySQL.
- `.env.example` là mẫu tham khảo; Spring đọc biến môi trường, không tự nạp file `.env`. Xem hướng dẫn Workbench để đặt biến trong PowerShell.
- [API Contract](docs/API_CONTRACT.md), [ERD](docs/ERD.md), [truy vết7UC](docs/REQUIREMENTS_TRACEABILITY.md), [quyết định/sửa lỗi](docs/DECISIONS_AND_FIXES.md).
- [Nguồn bảo toàn](docs/reference/INDEX.md), [cần xác nhận](docs/REVIEW_REQUIRED.md), [báo cáo triển khai](docs/IMPLEMENTATION_REPORT.md), [kế hoạch commit](docs/COMMIT_PLAN.md).
- [MySQL Workbench](docs/MYSQL_WORKBENCH_SETUP.md): chỉ thực hiện sau nghiệm thu H2; SQL đã chuẩn bị nhưng chưa đồng nghĩa kiểm thử MySQL thành công.
