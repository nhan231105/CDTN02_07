# Quyết định và sửa lỗi theo nguồn yêu cầu

Nguồn ưu tiên: `PROMPT_AI_AGENT_7UC.md`, Sprint 2 trong workbook đúng đề tài, SRS. Trạng thái kiểm chứng xem `TEST_REPORT.md`; danh sách này mô tả quyết định implementation, không tự chứng nhận PASS.

| Vấn đề baseline / mâu thuẫn | Quyết định và lý do |
|---|---|
| Security `permitAll`, không JWT nhưng SRS yêu cầu quyền | JWT stateless một giờ; subject user ID; kiểm tra user active và tokenVersion mỗi request. Public chỉ auth/health/dữ liệu công khai. Không thêm refresh token ngoài phạm vi. |
| Client tự gửi employerId/studentId/userId | API canonical lấy danh tính từ JWT. Alias cũ còn giữ nhưng ID nếu gửi phải khớp principal; không dùng ID này để cấp quyền. |
| Đăng ký có thể nhận ADMIN | Chỉ STUDENT/EMPLOYER; BCrypt, kiểm tra mật khẩu theo SRS; transaction tạo profile theo role. Đăng ký không tự đăng nhập. |
| Không tạo hồ sơ, frontend chỉ đọc localStorage | Tạo profile khi đăng ký; GET/PUT `/me/profile` dùng dữ liệu DB; UI đọc lại sau lưu. |
| CV chỉ là chuỗi link | Metadata DB và file riêng ngoài static; PDF thực ≤5 MiB, filename lưu UUID. CV trong đơn là snapshot bất biến, không xóa CV cũ khi thay file hồ sơ. |
| Sprint yêu cầu upload cả CV và ảnh | Bổ sung avatar PNG/JPEG ≤2 MiB; kiểm tra nội dung ảnh, upload/download có auth. |
| Backlog đổi account có giới hạn 48 giờ | Endpoint riêng xác minh mật khẩu hiện tại; khóa row user khi cập nhật để hai request không vượt giới hạn; áp dụng mọi thay đổi email/SĐT/mật khẩu; đổi mật khẩu thu hồi token cũ bằng tokenVersion. |
| Job filters trả sớm từng nhánh | Kết hợp filter bằng AND; chỉ OPEN, không removed, chưa hết hạn ở danh sách public; cap12 và sort ổn định. |
| Vai trò employer/number of hires chưa rõ | Cho nhiều APPROVED; employer chủ động OPEN→FILLED sau ít nhất một APPROVED, rồi FILLED→COMPLETED. Không tự FILLED ngay khi duyệt. |
| Ứng tuyển trùng hoặc hai quyết định đồng thời | UNIQUE(job,student) + transaction/row locks; chỉ một request thắng, xung đột409; decision và notification atomic. |
| Review contract dùng application, source dùng job | Canonical POST `/applications/{id}/reviews`, suy ra job/reviewer/reviewee từ quan hệ. Giữ chiều employer→student và endpoint cũ nếu đáp ứng quyền tương đương. |
| Chuông thông báo chưa tích hợp | API lấy principal, ownership cả GET/PATCH; badge từ DB; polling30s khi tab hiển thị, refetch sau thao tác. |
| RuntimeException thành500, API client parse cả204 | Error handler JSON thống nhất; client có xử lý204, multipart/blob,401/403. Không log mật khẩu/token. |
| H2 dev và MySQL deployment trộn lẫn | `dev-h2` mặc định, seed chỉ dev; MySQL dùng schema có kiểm soát/validate, chưa tự chuyển DB. |
| Cổng Docker8080 nhưng server8082 | Đồng bộ cổng8082; frontend dùng URL môi trường hoặc Vite proxy `/api`. |
| Stakeholder có tiền kiểm duyệt nhưng Sprint cho đăng tin ngay | Trong 7 UC, OPEN hiển thị ngay; giữ admin gỡ vi phạm, không thêm hàng đợi duyệt mới. |
| Nhóm07/08, năm bìa, lịch sai | Giữ nguyên bản nguồn, ghi `REVIEW_REQUIRED.md`; không biến giả định thành thông tin chính thức. |
| Validation độ dài trước khi trim | Kiểm tra cả giá trị chuẩn hóa: tên ít nhất3 ký tự, tiêu đề ít nhất5, mô tả ít nhất20; khoảng trắng không giúp vượt validation. Có test hồi quy cho đăng ký, hồ sơ và tạo/sửa tin. |
| Kiểm tra đã ứng tuyển chỉ đọc trang đầu | Frontend kiểm tra lần lượt các trang lịch sử và hủy cập nhật state cũ khi đổi job/user; đơn ngoài12 mục đầu vẫn hiển thị đúng trạng thái sau reload. |
| Admin stats500 trong lần browser test đầu | Log xác nhận `NoClassDefFoundError: AdminStatsResponse` khi Maven đang biên dịch lại `target/classes` của backend chạy từ classpath. Chuyển runtime nghiệm thu sang JAR đã package để tách khỏi quá trình build; chạy lại API/admin/E2E trên JAR. Không sửa DTO để che lỗi môi trường. |
| Fixture test `@MapsId` ban đầu lỗi | Sửa fixture tạo user và profile trong cùng transaction, để JPA quản lý khóa dùng chung như luồng đăng ký thực. Giữ toàn bộ assertion nghiệp vụ và chạy lại suite. |
| IntelliJ mất phản hồi làm ngắt phiên chạy | Giữ source và evidence, kiểm tra hash JAR cùng kết quả đã lưu, rồi khôi phục đúng hai dịch vụ H2/frontend; không reset Git hoặc thay đổi MySQL để khôi phục. |

Giữ Java21/Spring Boot3.3.3, ReactJS/Vite/CSS và cấu trúc repository. Không tạo dự án mới, không nâng major framework, không xóa lịch sử Git. UI và API dùng H2 thật, không dùng JSON frontend giả kết quả.
