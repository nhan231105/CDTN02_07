# CampusWork trước khi merge

## Tệp thay đổi theo nhóm

| Nhóm | Tệp |
| --- | --- |
| Xác thực backend | `AuthController.java`, `AccountEmailController.java`, `AccountEmailService.java`, `CampusEmail.java`, `EmailChallenge.java`, `EmailChallengeRepository.java`, `UserServiceImpl.java`, `SecurityConfig.java` |
| Ứng tuyển và hội thoại backend | `ApplicationServiceImpl.java`, `ApplicationRepository.java`, `ApplicationMessageController.java`, `ApplicationMessageService.java`, `ApplicationMessage.java`, `ApplicationMessageRepository.java` |
| Tài khoản frontend | `Login.jsx`, `Register.jsx`, `ForgotPassword.jsx`, `StudentProfile.jsx`, `EmployerProfile.jsx`, `PasswordInput.jsx`, `OtpInput.jsx`, `format.js` |
| Ứng tuyển và hội thoại frontend | `Applicants.jsx`, `Applications.jsx`, `Notifications.jsx`, `Conversation.jsx`, `App.jsx`, `apiService.js`, `auth.js` |
| Giao diện và thương hiệu | `App.css`, `theme.css`, `index.html`, `public/campuswork-logo-khong-nen.png`, `public/campuswork-{16,32,48,180}.png`. Các kích thước favicon lấy từ PNG gốc sau khi cắt lề trong suốt. |
| DB, cấu hình và tài liệu | `schema.sql`, migration 07 và 08, `.env.example`, `docker-compose.yml`, `README.md`, `API_CONTRACT.md` và tài liệu này |
| Kiểm thử | `BusinessIntegrationTest.java`, `SecurityProfileIntegrationTest.java`, `PartTimeFeaturesTest.java`, `E2eApplication.java`, `frontend.test.jsx`, `fullstack.spec.js`, `playwright.config.js`, `api_smoke.py` |

## Luồng đăng ký và khôi phục tài khoản

Người dùng chọn vai trò, điền biểu mẫu và yêu cầu mã xác minh email. Một bản ghi trong `email_challenges` giữ trạng thái chờ. Backend tạo `users` và hồ sơ tương ứng trong cùng giao dịch sau khi xác minh. Tài khoản hiện có tiếp tục dùng trạng thái cũ.

Mỗi mã có 6 chữ số, hiệu lực 5 phút và tối đa 5 lượt nhập sai. Cho phép tối đa 4 lượt gửi trong một giờ cho mỗi email và mục đích, với ít nhất 60 giây giữa hai lần gửi. Mã được lưu bằng BCrypt, bị thay thế khi gửi lại và bị xóa khỏi bản ghi sau khi dùng thành công.

Đặt lại mật khẩu kết hợp OTP với mật khẩu mới trong một lần xác nhận. Backend lưu mật khẩu bằng BCrypt và tăng `users.token_version` để các JWT cũ hết hiệu lực. Màn hình yêu cầu đặt lại mật khẩu trả thông báo chung. Nếu email chưa đến, đợi đủ 60 giây rồi thử lại.

## Ứng tuyển, thông báo và liên hệ

Backend giữ các trạng thái `PENDING`, `APPROVED`, `REJECTED` và `WITHDRAWN`. Mốc nhà tuyển dụng đã xem hồ sơ tiếp tục dùng `viewed_at`. Lời mời phỏng vấn riêng chưa được triển khai trong luồng xét duyệt.

Nộp đơn tạo thông báo cho cả hai bên. Nhà tuyển dụng mở trang ứng viên của công việc từ thông báo. Sinh viên nhận xác nhận hoặc kết quả xét duyệt và có thể mở hội thoại từ thông báo được nhận. Thông báo giữ thời gian và trạng thái đã đọc trong bảng hiện có.

Sau khi giao dịch ứng tuyển hoặc xét duyệt commit, backend gửi email qua Brevo tới địa chỉ tài khoản. Mỗi email có khóa idempotency riêng theo sự kiện và người nhận. Nếu gửi thất bại, backend ghi log sự kiện và giữ kết quả nghiệp vụ cùng thông báo nội bộ. Email ứng tuyển hiện không có hàng đợi hoặc retry tự động.

Nếu Brevo từ chối yêu cầu OTP hoặc không xác nhận tiếp nhận, API trả `503 EMAIL_UNAVAILABLE`. Mã đó không dùng được và giới hạn gửi lại vẫn áp dụng. Brevo tiếp nhận email không đồng nghĩa thư đã tới hộp thư.

Mỗi đơn được duyệt có một hội thoại xác định bằng ID đơn. Tin nhắn lưu trong `application_messages`, có người gửi, người nhận, thời điểm gửi và thời điểm đọc. Frontend cập nhật khi mở trang, khi tab trở lại và mỗi 10 giây khi đang xem. API kiểm tra vai trò và quyền sở hữu đơn trong mọi thao tác.

Xem [hợp đồng API](API_CONTRACT.md) để biết payload và mã trạng thái.

## Triển khai Render và Railway

1. Sao lưu Railway MySQL. Trên bản sao kiểm thử, chạy các migration còn thiếu theo thứ tự, sau đó chạy [07](../db/mysql/migration_07_email_challenges.sql) và [08](../db/mysql/migration_08_application_messages.sql). Hai migration thêm hai bảng và không sửa bảng cũ. Với DB mới, dùng [schema.sql](../db/mysql/schema.sql) và điều chỉnh tên database trong câu lệnh nếu Railway cấp tên khác. Backend dùng Hibernate `validate` nên cần migration trước khi deploy.
2. Trong Render Web Service của backend, giữ Docker build ở root repo và health check `/api/auth/health`. Đặt `SPRING_PROFILES_ACTIVE=mysql,render`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS=https://campuswork-vlu.onrender.com` và `UPLOAD_DIRECTORY` theo cấu hình lưu trữ hiện có.
3. Cấu hình `BREVO_API_KEY` và `CAMPUSWORK_EMAIL_FROM` bằng sender đã xác minh trong Brevo, ví dụ `CampusWork <campuswork.system@gmail.com>`. Dịch vụ gửi qua HTTPS. [Tài liệu Brevo](https://developers.brevo.com/docs/send-a-transactional-email).
4. Trong Render Static Site, đặt root `freelance-frontend`, build `npm ci --include=dev && npm run build`, publish `dist`, Node 24 và `VITE_API_BASE_URL=https://campuswork-platform.onrender.com/api`. Giữ rewrite `/*` tới `/index.html` để refresh URL con. Deploy lại frontend sau khi đổi biến Vite.
5. Xác nhận health và CORS trước khi chạy các luồng bên dưới trên môi trường staging. Dùng email thử do nhóm kiểm soát.

## Kiểm tra website

1. Mở trang đăng ký ở desktop và mobile, chuyển vai trò bằng bàn phím, bật/tắt mắt mật khẩu và kiểm tra nội dung nhập còn nguyên.
2. Đăng ký sinh viên và nhà tuyển dụng bằng hai email khác nhau. Thử đăng nhập trước xác minh, mã sai, gửi lại sớm, mã hết hạn và mã cũ sau khi cấp mã mới. Hoàn tất xác minh và thử đăng nhập bằng mật khẩu đúng và sai.
3. Mở **Quên mật khẩu?**, nhập email, dùng OTP để đổi mật khẩu và đăng nhập lại. Kiểm tra JWT từ tab cũ hết hiệu lực, mã đã dùng không thể dùng tiếp và email lạ nhận cùng thông báo yêu cầu.
4. Nhà tuyển dụng đăng tin. Sinh viên tải CV, nộp đơn, mở thông báo xác nhận. Từ chuông của nhà tuyển dụng mở đúng danh sách ứng viên và xem CV.
5. Thử duyệt một đơn và từ chối một đơn khác. Kiểm tra nội dung thông báo, trạng thái đã đọc và số lượng thông báo sau khi refresh.
6. Từ đơn được duyệt, mở nút liên hệ bằng cả hai tài khoản. Gửi tin nhắn hai chiều, mở lại trang để kiểm tra lịch sử, giờ gửi và trạng thái đọc. Dùng tài khoản thứ ba hoặc đơn chưa duyệt để kiểm tra API trả 403.
7. Kiểm tra favicon và tiêu đề trên Chrome/Edge, refresh các URL công việc, thông báo và hội thoại, rồi kiểm tra bản mobile của danh sách việc, biểu mẫu và hồ sơ.

## Kiểm thử local

`mvn verify` đã qua 43 bài kiểm thử, Vitest qua 31 và REST smoke qua 104 kiểm tra. Playwright qua 7 bài trên Edge và 4 bài chạy trên Chrome. Backend đã khởi động và trả health 200 với các migration trên MySQL 8.0.46 cục bộ, profile `mysql,render`. Build frontend đã kiểm tra với URL production. Lint có một cảnh báo trong `MyAvatar.jsx` và Vite báo bundle chính lớn hơn 500 kB.

Email được kiểm thử qua hộp thư thử nghiệm. Nhóm cần xác nhận gửi và nhận bằng Brevo thật, áp dụng migration trên bản sao Railway và chạy smoke sau deploy trước khi đưa bản này vào production.

`mvn verify` chạy backend với H2. Trong `freelance-frontend`, chạy `npm ci`, `npm run lint`, `npm test` và `npm run build`.

Để chạy Playwright và REST smoke với email giả lập, đặt `CAMPUSWORK_E2E_OUTBOX` thành đường dẫn tuyệt đối tới thư mục riêng trong `target` và đặt `APP_DEMO_PASSWORD` bằng mật khẩu test. Khởi động backend test bằng `mvn spring-boot:test-run -Dspring-boot.run.main-class=org.example.E2eApplication` từ root repo và Vite bằng `npm run dev`. Trong terminal Playwright đặt cùng hai biến rồi chạy `npm run test:e2e`. Có thể đổi trình duyệt bằng `CAMPUSWORK_E2E_BROWSER=chrome` và URL bằng `CAMPUSWORK_E2E_FRONTEND`. REST smoke dùng `python scripts/api_smoke.py --report target/api-smoke.json` với cùng cấu hình. Hộp thư thử nghiệm chỉ có trong test classpath.

## Pull Request

Mở checkout `vanle`, chạy `git status` và `git diff --check`, xem diff rồi stage và commit các file đã review. Push bằng `git push origin vanle`. Trong GitHub mở **New pull request**, chọn base `main` và compare `vanle`. Đính kèm kết quả QA, thứ tự migration và kết quả gửi email thật từ staging trước khi merge.
