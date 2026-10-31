# Thông tin cần chủ dự án xác nhận

Các điểm dưới đây không chặn triển khai kỹ thuật 7 UC. Bản gốc được giữ nguyên trong `reference/`; không tự sửa lịch chính thức, thông tin nhóm hoặc tuyên bố đã được phê duyệt.

| Nội dung | Bằng chứng | Đề nghị xác nhận |
|---|---|---|
| Số nhóm | SRS ghi **NHÓM 07**, báo cáo Chuyên đề ghi **Nhóm 8** | Số nhóm chính thức dùng cho bìa và biểu mẫu |
| Năm bìa | Báo cáo ghi 2025; kế hoạch và nội dung triển khai có mốc 2026 | Năm nộp báo cáo chính thức |
| Lịch Sprint | Workbook đúng đề tài có ngày bắt đầu 2027…2032 nhưng ngày kết thúc chủ yếu 2026 | Lịch đã được giảng viên chấp thuận |
| Workbook tại root | Nội dung phân công giảng dạy/thêm học kỳ, khác marketplace | Có cần thay file nộp chính thức bằng workbook đúng đề tài không |
| Phạm vi quản trị | SRS có FR-13…15; Sprint chỉ giao 7 UC sinh viên và các phụ thuộc employer | Xác nhận phần quản trị mở rộng thuộc đợt tiếp theo |

## Đề xuất sửa lịch, chưa áp dụng vào bản gốc

| Hàng / task Sprint 2 | Bất hợp lý | Đề xuất |
|---|---|---|
| 25 / S-2.1 | Cần kiểm tra cùng mốc hoàn thành 2026 | Giữ ngày/tháng, đối chiếu lại năm với lịch lớp |
| 26 / S-2.2 | Bắt đầu 26/10/2027, kết thúc 28/10/2026 | Đề xuất bắt đầu 26/10/2026 nếu đúng lịch lớp |
| 27…31 / S-2.3…S-2.7 | Năm bắt đầu tăng dần đến 2032, kết thúc lại ở 2026 | Đề xuất cùng năm 2026; người phụ trách xác nhận thứ tự và thời lượng |

**Ghi chú lịch sử:** Trong giai đoạn triển khai 7 Use Case ban đầu, dự án sử dụng H2 và chưa nghiệm thu kết nối MySQL thực tế. Đến ngày 31/10/2026, nhóm đã hoàn tất kết nối và nghiệm thu MySQL 8.0 trên localhost. Kết quả được trình bày trong mục cập nhật bên dưới. Chưa thực hiện chuyển đổi tự động dữ liệu từ H2 sang MySQL.

## Cập nhật nghiệm thu MySQL — 31/10/2026

Dự án CampusWork đã kết nối và nghiệm thu MySQL 8.0 trên môi trường localhost.

- Đã tạo 9 bảng theo `db/mysql/schema.sql`.
- Đã nạp dữ liệu mẫu bằng `db/mysql/seed_demo.sql`.
- Backend Spring Boot đã kết nối MySQL thành công.
- Đã tạo tin tuyển dụng qua giao diện React và xác nhận bản ghi tồn tại trong MySQL.
- Dữ liệu vẫn tồn tại sau khi khởi động lại Backend.
- Giao diện hiển thị đúng tiếng Việt.

H2 tiếp tục được giữ lại cho môi trường phát triển và kiểm thử tự động.

Chi tiết kết quả được ghi trong `docs/TEST_REPORT.md` và `docs/MYSQL_WORKBENCH_SETUP.md`.

Lưu ý: Chưa thực hiện di chuyển tự động toàn bộ dữ liệu từ H2 sang MySQL.