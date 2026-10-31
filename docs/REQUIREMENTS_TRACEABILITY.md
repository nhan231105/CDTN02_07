# Truy vết 7 Use Case

Trạng thái nghiệm thu được quyết định bằng [TEST_REPORT.md](TEST_REPORT.md). Chỉ ghi DONE khi đủ UI + API + H2 + security + test dương/âm + E2E. Trong quá trình triển khai, các dòng chưa có evidence vẫn là **PENDING**, kể cả source đã viết xong.

| UC | FR / PB / AC | Màn hình | API canonical | Dữ liệu | Kiểm thử / evidence | Trạng thái |
|---|---|---|---|---|---|---|
| UC_01 Login | FR-02 / PB-01.2 | Login; route guard theo role | POST auth/login; GET me/profile | users, token_version | Backend auth tests; api_smoke UC01; E2E role routing/logout | Xem TEST_REPORT |
| UC_02 Register | FR-01 / PB-01.1 / AC-1.1.2 | Register STUDENT/EMPLOYER | POST auth/register | users + profile tương ứng | Đúng201, input400, trùng409, ADMIN400; UI validation | Xem TEST_REPORT |
| UC_03 Profile/CV/avatar/account | FR-03 / PB-01.3 / AC-1.3.1…3 | Hồ sơ; upload; đổi tài khoản | GET/PUT me/profile; POST/GET me/cv; POST/GET me/avatar; PATCH me/account | profiles, stored_files, users | PDF thật/giả/size; ownership; reload; CV snapshot;48h | Xem TEST_REPORT |
| UC_04 Search | FR-06 / PB-03.1 / AC-3.1.2…4 | Khám phá; chi tiết; bộ lọc; phân trang | GET jobs; GET jobs/{id} | jobs, users/employer_profiles, reviews | AND filters,12/page, query URL, empty/errors; latency/browser responsive | Xem TEST_REPORT |
| UC_05 Apply | FR-07 / PB-03.2 | Form xác nhận; lịch sử đơn | POST jobs/{id}/applications; GET me/applications | applications UNIQUE(job,student), stored_files |201/409, role/closed/deadline/consent/CV, concurrent duplicate | Xem TEST_REPORT |
| UC_06 Review employer | FR-10 / PB-05.1 | Lịch sử đơn; form sao/bình luận | POST applications/{id}/reviews; GET reviews/jobs/{id} | reviews, applications, jobs | Approved+completed+owner; rating1–5; duplicate409; reload | Xem TEST_REPORT |
| UC_07 Notifications | FR-12 / PB-06.1 | Chuông/badge; danh sách | GET me/notifications; GET count/unread; PATCH {id}/read | notifications | Recipient/transaction/read persistence; cross-user403; polling UI | Xem TEST_REPORT |
| Phụ thuộc employer | FR-04/05/08/09 / PB-02/04 | Dashboard tin; ứng viên; quyết định; trạng thái | POST/PUT jobs; GET jobs/employer/{id}; GET jobs/{id}/applications; PATCH applications/{id}/decision; PATCH jobs/{id}/status | jobs, applications, notifications | Owner vs non-owner, state transition, concurrent decision, E2E | Xem TEST_REPORT |

## Nguồn

- Phạm vi ưu tiên: `PROMPT_AI_AGENT_7UC.md` và `reference/KEHOACH_cdtn2-06_2374802010360_VoNguyenThanhNhan.xlsx`, sheet kế hoạch hàng25–31.
- Chức năng/NFR: `reference/SRS_Rut_gon.md`; tiêu chí chi tiết và usecase trong workbook.
- API đề xuất và sơ đồ: `reference/API_Contract.md`, `reference/SoDoKienTruc.png`, `reference/LuongNghiepVu.png`.
- Hợp đồng triển khai: `API_CONTRACT.md`; schema chuẩn bị MySQL: `../db/mysql/schema.sql`.

FR-11 được giữ nếu backend hiện hữu đã hỗ trợ; không tuyên bố hoàn thành toàn bộ UI chiều employer→student. FR-13…15 và các tính năng thanh toán/chat ngoài phạm vi 7 UC không được đánh dấu DONE bằng đợt bàn giao này.
