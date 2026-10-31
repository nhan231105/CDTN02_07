# Báo cáo kiểm thử thực thi

Ngày triển khai: 31/10/2026. Báo cáo này chỉ ghi bằng chứng thực chạy; source hoặc test vừa viết không đồng nghĩa PASS.

## Baseline trước thay đổi

| Hạng mục | Kết quả thực tế |
|---|---|
| Git | Working tree sạch trước triển khai |
| Backend Maven test | BUILD SUCCESS;71 Java compile; **No tests to run**, không phải đã kiểm thử nghiệp vụ |
| Frontend build | FAIL: chưa có dependency Vite (`vite is not recognized`) |
| Công cụ | Java21, Node24; Edge đã có trên máy |

## Kết quả sau triển khai

Backend integration/package, REST, H2 restart, frontend và browser E2E **đã có kết quả PASS** trong evidence chạy với `dev-h2`. Kết nối MySQL local đã được nghiệm thu **thủ công** sau đó; không gộp các lần chạy ở hai profile thành một bộ test.

| Bộ kiểm thử | Trạng thái | Evidence |
|---|---|---|
| Backend unit/integration H2, auth/file/ownership/concurrency + package | **PASS29/29**,0fail/errors/skips; deployable JAR | `mvn -B -ntp package`,16:32:19+07; `evidence/backend-tests.json`, `evidence/backend-tests.log` |
| API7UC trên HTTP thật | **PASS101/101**,0fail | `python -B scripts/api_smoke.py`,16:24:23+07; `evidence/api-smoke.json` |
| Frontend lint/build/unit/format | **PASS23/23**,0fail; lint/build/format CHECK PASS | `evidence/frontend-checks.json`, ghi lại sau recovery lúc16:40(+07) |
| Browser E2E và responsive360/768/1440 | **PASS6/6**, 0 skipped/unexpected/flaky trên Edge headless + `dev-h2` | `evidence/browser-e2e.json`, screenshots `evidence/jobs-*.png` |
| NFR API danh sách≤3s | **PASS** trong môi trường đo |30 mẫu: mean28.70ms,p95=34.18ms,max34.39ms; HTTP loopback Spring Boot dev-h2/Java21, không gồm browser render |
| NFR browser render≤3s | **PASS trong ca thử Edge/dev-h2**: 490–555ms với 360/768/1440px | `evidence/browser-e2e.json`; không suy ra kết quả MySQL hoặc mọi thiết bị |
| H2 restart deterministic seed | **PASS** | `evidence/h2-restart.json`,16:30:53+07;5accountID giữ nguyên,24tin=16OPEN/4FILLED/4COMPLETED;2employer mỗi người12tin; QA data trước restart mất đúng thiết kế |
| MySQL/Workbench | **PASS kiểm tra tích hợp thủ công cơ bản**: 9 bảng, seed 5 users/24 jobs; JDBC, backend profile mysql; API + SQL xác nhận job25 vẫn tồn tại sau restart, 17 OPEN | Log/ảnh và truy vấn người thực hiện cung cấp ngày31/10/2026; **chưa chạy full suite trên MySQL** |

### Phạm vi PASS hiện có

- Suite `SecurityProfileIntegrationTest`:13 test, bao gồm hashed registration+profile, normalization/phone login, role/ownership, JWT thiếu/giả/hết hạn, account khóa sau cấp token, file thật/giả/rỗng/quá cỡ, avatar, giới hạn48h kể cả cập nhật đồng thời, chuẩn hóa họ tên và đổi mật khẩu thu hồi token.
- Suite `BusinessIntegrationTest`:16 test, AND filters/cap12, apply/history/role/CV/deadline/removed, lifecycle, review điều kiện+unique, concurrent apply/decision/review, immutable job sau có ứng viên, CV snapshot, validation sau trim và rollback khi notification lỗi.
- REST script:101 assertion trên HTTP thật, account/job QA độc lập, upload PDF thật và ảnh; kiểm HTTP theo contract. Luồng thực đã đi qua đăng ký→profile/CV→search→apply→approve→notification→FILLED/COMPLETED→review, đọc lại DB qua REST.

Đã sửa lỗi fixture test `@MapsId` trong quá trình chạy; sửa thêm lỗi cạnh tranh vượt giới hạn48h và validation tên sau trim, rồi regression cuối29/29 PASS. Không có lỗi nghiệp vụ đang bị che bằng việc tắt test/validation. Kết quả API101 không bao gồm backend29, không cộng chúng thành coverage100%.

## Quy tắc evidence

- PASS chỉ khi lệnh/assertion hoàn thành thành công. FAIL được giữ lại cùng nguyên nhân/sửa lỗi và lần regression cuối.
- BLOCKED phải có blocker cụ thể; NOT RUN là chưa chạy, không phải đã đạt.
- Không ghi token, mật khẩu người dùng hoặc file CV thật vào log; account demo chỉ dev đã được mô tả trong README.
- API smoke tạo account/job QA riêng trong H2 để không phá seed. Dữ liệu QA mất sau restart đúng thiết kế H2.
- Latency loopback HTTP không chứng minh thời gian render trình duyệt; kết quả MySQL không được suy ra từ H2.

## Nghiệm thu MySQL local sau khi xuất evidence H2 (31/10/2026)

Các bước sau thực hiện trên máy Windows với MySQL Server tại `127.0.0.1:3306`, **không nằm trong** các file JSON evidence `dev-h2` phía trên:

1. MySQL Workbench chạy `schema.sql` thành công, xác nhận 9 bảng. `seed_demo.sql` tạo 5 người dùng mẫu và 24 việc làm.
2. CLI MySQL bằng `freelance_app@localhost` và ứng dụng JDBC Java21 đọc được 24 jobs. Các quyền schema: SELECT, INSERT, UPDATE, DELETE.
3. Backend Spring Boot3.3.3, JDK21, `SPRING_PROFILES_ACTIVE=mysql` chạy thành công sau khi truyền `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`; HikariCP + JPA khởi tạo, Tomcat port8082.
4. Website tạo tin ID25 `QA MySQL Persistence 20261031`, status OPEN; Workbench xác nhận tổng 25 jobs. Sau restart backend, `GET /api/jobs/25` vẫn trả ID25 và danh sách công khai trả 17 OPEN jobs; giao diện hiển thị tiếng Việt đúng dấu.

**Giới hạn:** Chưa có evidence chạy lại 101 assertions API, 29 bài test backend hay 6 E2E với cấu hình MySQL mới; cũng chưa có benchmark hiệu năng MySQL/concurrency và bài test backup-restore. Kết quả UI thủ công không được hiểu là bộ kiểm thử tự động MySQL đã PASS.
