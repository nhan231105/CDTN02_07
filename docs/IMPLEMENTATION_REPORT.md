# Báo cáo triển khai 7 UC

Đợt triển khai mở rộng repository hiện hữu; không tạo dự án mới, không thay framework và không commit/push. Tình trạng test cuối cùng xem [TEST_REPORT.md](TEST_REPORT.md).

## Các nhóm thay đổi

- Backend: JWT/principal/ownership, đăng ký kèm profile, H2 seed, validation/error handler, metadata và kho file riêng, API 7 UC cùng các thao tác employer phụ thuộc.
- Business: filter kết hợp, pagination12, chống trùng/concurrent apply/decision, notification transaction, review theo application và lịch sử đơn.
- Frontend: route thật, session bootstrap, API JSON/multipart/blob, giao diện responsive, hồ sơ/CV/avatar/account, jobs/apply/review/notification và employer workflow.
- QA: backend integration, frontend unit, API smoke stdlib, browser E2E; ghi số liệu và limitations sau chạy.
- Tài liệu: nguồn gốc được bảo toàn, contract/ERD/traceability/decision/review/SQL/Workbench/commit plan.

## Phạm vi nghiệm thu

UC_01…UC_07 và employer support tối thiểu. Admin đăng nhập/role gate/dashboard giữ tương thích. Không xem FR-13…15, thanh toán hoặc chat nâng cao là hoàn tất trong đợt này.

## Bằng chứng và giới hạn

Backend package/regression29/29, REST acceptance101/101 và H2 restart đã PASS; evidence được ghi trong TEST_REPORT. Frontend **23/23** tests, lint, build, format đều PASS (evidence `frontend-checks.json`); Edge/Playwright **6/6** PASS (evidence `browser-e2e.json`). Các bằng chứng tự động này được ghi khi chạy profile `dev-h2`, không phải kết quả chạy lại toàn bộ suite trên MySQL. H2 là in-memory: khởi động lại quay về seed.

### Kết quả tích hợp MySQL local — xác nhận thủ công 31/10/2026

- Tạo schema `freelance_marketplace` có **9 bảng** bằng `db/mysql/schema.sql`; seed **5 tài khoản demo** (2 STUDENT, 2 EMPLOYER, 1 ADMIN) và **24 jobs** (16 OPEN, 4 FILLED, 4 COMPLETED) bằng `seed_demo.sql`.
- Tài khoản ứng dụng `freelance_app@localhost` được cấp SELECT, INSERT, UPDATE, DELETE trên riêng schema; kết nối bằng MySQL CLI TCP/IP và JDBC trực tiếp thành công, truy vấn đọc được 24 jobs.
- Backend chạy Java 21, Spring profile `mysql`, HikariCP kết nối MySQL, JPA EntityManagerFactory khởi tạo và Tomcat nghe cổng 8082; frontend Vite phục vụ cổng 5173.
- Từ giao diện Nhà tuyển dụng tạo tin `QA MySQL Persistence 20261031` ID **25**, trạng thái `OPEN`. SQL xác nhận tổng 25 jobs. Sau khi khởi động lại backend, API `GET /api/jobs/25` trả đúng tin ID 25 và danh sách công khai có **17 OPEN jobs**.
- Trang chi tiết trên Edge hiển thị đúng dấu tiếng Việt. Đây là **nghiệm thu đọc/ghi và tính bền vững MySQL theo ca thử thực tế**; chưa thay thế bài test đầy đủ cho MySQL (101 API assertions/6 E2E chưa được báo cáo chạy lại với profile `mysql`).

