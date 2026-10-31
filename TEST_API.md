# REST acceptance — 7 Use Case

Bản hướng dẫn cũ được bảo toàn ở [reference baseline](docs/reference/repository-baseline/TEST_API.md). Contract hiện hành: [API_CONTRACT](docs/API_CONTRACT.md).

## Chạy kiểm thử thật

Khởi động backend H2 theo README. Với phiên nghiệm thu, dùng `java -jar target/freelance-marketplace-1.0.0.jar --spring.profiles.active=dev-h2` sau khi `mvnw.bat package` hoàn thành, sau đó ở terminal khác:

```powershell
python -B scripts/api_smoke.py --base-url http://localhost:8082/api --report docs/evidence/api-smoke.json
```

Script chỉ dùng Python stdlib, tạo account/job QA riêng trong H2, phát sinh PDF thật và gọi HTTP thật. Token chỉ được giữ trong bộ nhớ phiên chạy, không in hoặc ghi vào evidence.

Kiểm tra login email/SĐT và401/403, register201/400/409, profile lưu/đọc, CV/ảnh/file415/413, phân trang12 và AND filters, apply/duplicate/consent/ownership, employer approve, notification/read/cross-user, lifecycle, review/duplicate và cạnh tranh hai request. Script đo30 mẫu API loopback và xuất timing thực tế; thời gian render browser được đo riêng.

## Evidence và giới hạn

- [TEST_REPORT](docs/TEST_REPORT.md) ghi kết quả run cuối, frontend/backend/E2E và những gì chưa kiểm chứng.
- JSON evidence nằm trong `docs/evidence/`; mỗi assertion có PASS/FAIL, mã HTTP và duration khi phù hợp.
- Backend JUnit kiểm tra thêm JWT hết hạn/khóa tài khoản/rollback/concurrency; xem Surefire và TEST_REPORT.
- Browser E2E kiểm tra UI và responsive riêng. MySQL ghi NOT RUN cho đến khi kết nối thật.
- Dữ liệu QA mất sau restart đúng thiết kế H2. Nếu lần chạy thất bại, giữ evidence rồi sửa và chạy regression; giữ expected status theo contract nghiệp vụ.
