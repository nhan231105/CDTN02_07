# Phân tích Seed Data CampusWork

Phạm vi là bản checkout `campuswork-vanle-review`, nhánh `main`, commit `96ea785`. Báo cáo dựa trên Entity, schema MySQL, controller, service và frontend của cùng bản checkout. Các đề xuất dưới đây dành cho giai đoạn triển khai trên môi trường thử nghiệm. Chưa tạo bản ghi, cấp mật khẩu, tạo ảnh hay kết nối database.

## 1. Cấu trúc Entity và MySQL

Schema dùng InnoDB và khai báo `utf8mb4` cho database khi tạo mới. Các độ dài dưới đây tính theo khai báo schema. Các trường ghi **bắt buộc** là `NOT NULL`. Callback và giá trị khởi tạo Java chỉ có hiệu lực khi ghi qua ứng dụng. Profile MySQL dùng Hibernate `validate` và tắt SQL initializer, nên khởi động ứng dụng không tự nạp schema hoặc seed.

### `User` → `users`

| Cột MySQL | Kiểu và ràng buộc | Giá trị seed |
|---|---|---|
| `id` | BIGINT, PK, tự tăng | Để MySQL cấp |
| `full_name` | VARCHAR(100), bắt buộc | Tên người phụ trách giả lập |
| `email` | VARCHAR(150), bắt buộc, UNIQUE riêng | Email riêng cho từng công ty, trim và lowercase |
| `phone` | VARCHAR(20), bắt buộc, UNIQUE riêng | Số thử nghiệm riêng, theo định dạng API |
| `password_hash` | VARCHAR(255), bắt buộc | BCrypt của mật khẩu riêng |
| `role` | ENUM `STUDENT`, `EMPLOYER`, `ADMIN`, bắt buộc | `EMPLOYER` |
| `status` | ENUM `ACTIVE`, `LOCKED`, bắt buộc | `ACTIVE`, mặc định Java |
| `token_version` | INT, bắt buộc, SQL và Java mặc định 0 | 0 khi tạo |
| `lock_reason`, `contact_changed_at` | VARCHAR(500), DATETIME(6), nullable | NULL khi tạo |
| `created_at`, `updated_at` | DATETIME(6), chỉ `created_at` bắt buộc | JPA điền cả hai lúc tạo, cập nhật `updated_at` khi sửa |

`role`, `status` và timestamp không có SQL DEFAULT khai báo. Nếu ghi trực tiếp, cấp rõ các giá trị cần thiết. UNIQUE của email và số điện thoại áp dụng trên cả bảng, kể cả tài khoản sinh viên và admin.

### `EmployerProfile` → `employer_profiles`

| Cột MySQL | Kiểu và ràng buộc | Giá trị seed |
|---|---|---|
| `user_id` | BIGINT, PK và FK → `users.id` | ID tài khoản EMPLOYER, quan hệ 1–1 qua `@MapsId` |
| `org_name`, `tagline` | VARCHAR(150) mỗi cột | Tên công ty và thông điệp tiếng Việt |
| `address`, `website` | VARCHAR(255) mỗi cột | Địa chỉ mô phỏng, website giả lập nếu cần |
| `description` | TEXT | Giới thiệu tiếng Việt, tối đa 3.000 ký tự nếu qua `MeProfileRequest` |
| `industry`, `company_size` | VARCHAR(100), VARCHAR(50) | Ngành và quy mô, lưu chuỗi |
| `founded_year` | INT | API chấp nhận 1900–2100 |
| `benefits` | VARCHAR(600) | Chuỗi quyền lợi phân tách bằng ký tự pipe, API trả danh sách |
| `logo_ref`, `cover_ref` | VARCHAR(255) mỗi cột | Logo tham chiếu `stored_files.ref`. Ảnh bìa có thể NULL |

Ngoài PK, các cột hồ sơ cho phép NULL và không có mặc định nghiệp vụ ở schema. `org_name` không UNIQUE. Đăng ký thành công tạo hồ sơ và gán tên tổ chức ban đầu từ họ tên tài khoản. `logo_ref` và `cover_ref` là tham chiếu logic, schema không khai báo FK tại hai cột này.

### `Job` → `jobs`

| Cột MySQL | Kiểu, ràng buộc và mặc định |
|---|---|
| `id`, `employer_id` | BIGINT, PK tự tăng, FK bắt buộc → `users.id`. API lấy chủ tin từ JWT và kiểm tra vai trò |
| `title`, `description`, `deadline` | VARCHAR(200), TEXT, DATE. Cả ba bắt buộc, không có SQL DEFAULT |
| `category`, `location`, `work_schedule` | VARCHAR(100), VARCHAR(100), VARCHAR(200). SQL nullable, API tạo tin yêu cầu cả ba |
| `requirements`, `benefits` | TEXT, nullable. API giới hạn lần lượt 2.000 và 1.000 ký tự |
| `salary` | DECIMAL(12,0), SQL nullable với CHECK không âm khi có giá trị. API yêu cầu số nguyên không âm, tối đa 12 chữ số |
| `job_type`, `pay_type` | VARCHAR(20), bắt buộc, mặc định SQL và Java tương ứng `PART_TIME`, `HOURLY` |
| `shifts`, `hours_per_week` | VARCHAR(100) chứa danh sách tên enum cách nhau bằng dấu phẩy, INT nullable. API nhận tối đa 5 ca, 1–80 giờ mỗi tuần |
| `no_experience_required`, `student_friendly` | BOOLEAN bắt buộc, SQL và Java mặc định false và true |
| `address`, `start_date`, `duration_months` | VARCHAR(255), DATE, INT nullable. API giới hạn thời lượng 1–60 tháng |
| `headcount` | INT bắt buộc, SQL và Java mặc định 1, API chấp nhận 1–1000 |
| `lat`, `lng` | DOUBLE nullable. API giới hạn ±90 và ±180, tạo tin chỉ lưu tọa độ khi có đủ cặp |
| `status` | ENUM `OPEN`, `FILLED`, `COMPLETED`, bắt buộc. Java mặc định `OPEN`, SQL không khai báo DEFAULT |
| `removed_by_admin`, `removal_reason` | BOOLEAN bắt buộc mặc định false, VARCHAR(500) nullable |
| `created_at`, `updated_at` | DATETIME(6) bắt buộc. Hibernate tự điền qua `@CreationTimestamp` và `@UpdateTimestamp` |

Enum Java của loại việc là `PART_TIME`, `SEASONAL`, `INTERNSHIP`, `FREELANCE`. Enum đơn vị trả lương là `HOURLY`, `SHIFT`, `DAILY`, `MONTHLY`, `PROJECT`. Ca làm gồm `MORNING`, `AFTERNOON`, `EVENING`, `WEEKEND`, `FLEXIBLE`. Schema lưu loại việc, đơn vị lương và ca bằng VARCHAR. Seed trực tiếp cần tự kiểm tra tập giá trị. Các index theo trạng thái, địa điểm, ngành và employer chỉ tăng tốc truy vấn. Bảng chưa có UNIQUE dành cho định danh tin seed.

### `StoredFile` → `stored_files`

| Cột MySQL | Kiểu và ràng buộc | Giá trị cho logo |
|---|---|---|
| `ref` | VARCHAR(36), PK do ứng dụng cấp | UUID |
| `owner_id` | BIGINT bắt buộc, FK → `users.id` | ID tài khoản sở hữu logo |
| `original_name` | VARCHAR(255) bắt buộc | Tên file PNG/JPEG đã chuẩn hóa |
| `content_type` | VARCHAR(100) bắt buộc | `image/png` hoặc `image/jpeg` |
| `size` | BIGINT bắt buộc, CHECK ≥ 0 | Số byte thực tế, file upload phải có nội dung |
| `storage_name` | VARCHAR(100) bắt buộc, UNIQUE | Tên file vật lý, luồng chuẩn dùng UUID + đuôi |
| `created_at` | DATETIME(6) bắt buộc | Java khởi tạo thời điểm tạo, schema không có DEFAULT |

Hàng trong bảng chỉ chứa metadata. Logo hợp lệ cần cả `employer_profiles.logo_ref`, bản ghi đúng chủ sở hữu trong `stored_files`, và file thật trong thư mục upload của backend.

Nguồn tham chiếu · [schema.sql](../db/mysql/schema.sql), [User](../src/main/java/org/example/entity/User.java), [EmployerProfile](../src/main/java/org/example/entity/EmployerProfile.java), [Job](../src/main/java/org/example/entity/Job.java), [StoredFile](../src/main/java/org/example/entity/StoredFile.java).

## 2. Tài khoản, xác minh và kiểm duyệt

- **Đăng ký công khai.** `POST /api/auth/register` kiểm tra dữ liệu và yêu cầu gửi OTP. `POST /api/auth/register/verify` nhận `registration` và `code`, xác minh mã rồi mới gọi service tạo user và profile. OTP có 6 chữ số, hiệu lực 5 phút, gửi lại sau tối thiểu 60 giây, tối đa 4 lượt gửi trong một giờ và 5 lượt thử trên một mã. Mã dùng một lần và lưu dưới dạng hash trong `email_challenges`. DB không có trường `email_verified` trong `users`.
- **Dữ liệu tài khoản.** Tên sau trim từ 3–100 ký tự, email đúng định dạng và tối đa 150 ký tự, số điện thoại khớp `^0\d{9}$`. Mật khẩu 8–50 ký tự có chữ hoa, chữ thường và ký tự đặc biệt, tối đa 72 byte UTF-8. Đăng ký công khai cho phép `STUDENT` và `EMPLOYER`.
- **Mật khẩu và đăng nhập.** Spring cung cấp `BCryptPasswordEncoder`. Service gọi `encode` khi tạo tài khoản và `matches` khi đăng nhập bằng email hoặc số điện thoại. Mỗi công ty cần một mật khẩu ngẫu nhiên riêng. Chỉ đưa hash vào DB và chuyển mật khẩu qua kênh riêng ở giai đoạn triển khai. Khi chạy seed lại, giữ nguyên hash của tài khoản đã tồn tại.
- **JWT và quyền.** Sau khi mật khẩu khớp và tài khoản hoạt động, backend cấp JWT Bearer. Filter kiểm tra user hiện tại, trạng thái `ACTIVE` và phiên bản token, rồi cấp quyền `ROLE_EMPLOYER` từ role trong DB. Các thao tác đăng tin và tải logo yêu cầu `EMPLOYER`. Sửa tin và đổi trạng thái kiểm tra cả quyền sở hữu. Tài khoản `LOCKED` bị chặn ở đăng nhập và khi dùng JWT.
- **Phê duyệt tài khoản.** Sau khi xác minh email, tài khoản được tạo với `ACTIVE`. Schema không có trạng thái chờ admin duyệt. Admin có thể khóa và mở khóa, kèm lý do khi khóa. Nếu tạo fixture qua tiến trình nội bộ của môi trường thử nghiệm, dùng cùng quy tắc hash và tạo profile, không tạo kết quả OTP giả. Nếu cần kiểm thử luồng đăng ký thật, dùng hộp thư thử nghiệm nhận OTP.
- **Phê duyệt tin.** API tạo tin lưu `OPEN`, với `removed_by_admin=false` mặc định. Tin hiển thị công khai khi còn `OPEN`, chưa bị ẩn và deadline từ hôm nay trở đi theo múi giờ `Asia/Ho_Chi_Minh`. Không có bước duyệt trước khi đăng. Admin ẩn/khôi phục tin qua moderation/restore, thay đổi cờ và lý do, không tự gia hạn hoặc mở lại tin đã đóng. `APPROVED` là trạng thái của đơn ứng tuyển. Luồng chuyển tin sang `FILLED` yêu cầu có ít nhất một ứng viên đã được duyệt. Bộ lọc công khai hiện không kiểm tra `users.status`, vì vậy khóa user không tự ẩn tin.

Nguồn tham chiếu · [SecurityConfig](../src/main/java/org/example/config/SecurityConfig.java), [RegisterRequest](../src/main/java/org/example/service/RegisterRequest.java), [UserServiceImpl](../src/main/java/org/example/service/UserServiceImpl.java), [AccountEmailService](../src/main/java/org/example/service/AccountEmailService.java), [JwtAuthenticationFilter](../src/main/java/org/example/security/JwtAuthenticationFilter.java), [AdminServiceImpl](../src/main/java/org/example/service/AdminServiceImpl.java), [JobServiceImpl](../src/main/java/org/example/service/JobServiceImpl.java).

## 3. Avatar và logo công ty

- Avatar sinh viên dùng `student_profiles.avatar_ref` và `POST /api/me/avatar`. Avatar của nhà tuyển dụng trên giao diện dùng logo công ty. Upload logo qua `POST /api/me/logo`, dùng multipart trường `file`. Ảnh bìa dùng `/api/me/cover` và `cover_ref`.
- Logo chấp nhận PNG hoặc JPEG thật, MIME khớp nội dung, dung lượng tối đa 2 MiB và mỗi chiều tối đa 4096 px. Ảnh bìa tối đa 4 MiB. Service kiểm tra chữ ký file và khả năng giải mã, sinh UUID, ghi file với tên riêng rồi lưu metadata trước khi gắn ref vào hồ sơ. Có dọn file vừa tạo khi transaction rollback trong luồng upload.
- Ảnh công khai tải từ `GET /api/employers/{id}/logo`, ID là user ID. Backend tra ref, đọc bytes và trả đúng MIME kèm public cache 30 ngày. Frontend dùng logo ref làm phiên bản URL cho hồ sơ công ty, trang chi tiết việc và avatar nhà tuyển dụng. Nếu thiếu ref, metadata hoặc file thật thì endpoint ảnh không có ảnh để trả.
- Chuẩn bị 15 file PNG khác nhau, đề xuất 512 × 512 px, mỗi file thiết kế theo tên hoặc lĩnh vực công ty. Đặt file vào vùng lưu trữ bền vững của backend khi triển khai thử nghiệm. Sao lưu chỉ MySQL không phục hồi được bytes của logo. API upload hiện thay logo ref và tạo metadata mới mỗi lần gọi, nên tác vụ seed phải kiểm tra định danh trước khi gọi lại.

Nguồn tham chiếu · [MeController](../src/main/java/org/example/controller/MeController.java), [FileStorageService](../src/main/java/org/example/service/FileStorageService.java), [PublicEmployerController](../src/main/java/org/example/controller/PublicEmployerController.java), [brand.js](../freelance-frontend/src/utils/brand.js), [MyAvatar](../freelance-frontend/src/components/MyAvatar.jsx).

## 4. Tin part-time lương theo giờ

Payload mẫu cho `POST /api/jobs`. Ngày ở dạng quy ước tài liệu, khi thực thi cần thay bằng ISO `YYYY-MM-DD`. Backend lấy chủ tin từ JWT nên payload có thể bỏ `employerId`. Nếu truyền ID, nó phải khớp tài khoản đăng nhập.

```yaml
title: Nhân viên pha chế ca tối
category: Ăn uống
description: Chuẩn bị đồ uống, kiểm tra nguyên liệu và vệ sinh quầy sau mỗi ca.
requirements: Làm được ít nhất ba ca mỗi tuần.
benefits: Có hướng dẫn pha chế và hỗ trợ đổi ca.
salary: 32000
payType: HOURLY
jobType: PART_TIME
location: Quận 3, TP. Hồ Chí Minh
workSchedule: 18:00–22:00, ba buổi mỗi tuần
shifts: [EVENING]
hoursPerWeek: 12
noExperienceRequired: true
studentFriendly: true
headcount: 2
deadline: ngày chạy lần đầu + 45 ngày
startDate: ngày chạy lần đầu + 52 ngày
durationMonths: 3
```

`salary: 32000` là 32.000 đồng mỗi giờ. UI thêm hậu tố `/giờ` theo `payType`. Lương lưu dạng số nguyên đồng. DTO yêu cầu tiêu đề 5–200 ký tự, mô tả 20–5000 ký tự, ngành và địa điểm không rỗng tối đa 100 ký tự, lịch làm không rỗng tối đa 200 ký tự, và deadline từ hiện tại trở đi. Nếu seed trực tiếp vào DB, giữ các điều kiện này dù schema cho phép một số cột NULL. Tính ngày theo múi giờ Việt Nam khi tạo lần đầu. Tin hết hạn sau đó vẫn được giữ nguyên khi chạy lại.

Nguồn tham chiếu · [CreateJobRequest](../src/main/java/org/example/dto/CreateJobRequest.java), [JobController](../src/main/java/org/example/controller/JobController.java), [JobServiceImpl](../src/main/java/org/example/service/JobServiceImpl.java), [format.js](../freelance-frontend/src/utils/format.js).

## 5. Cấu trúc đầu vào và danh mục minh họa

Manifest dự kiến lưu Unicode UTF-8. Các mã bên dưới là định danh của manifest, schema hiện tại chưa có cột cho các mã này. Mỗi công ty gồm một tài khoản, một hồ sơ, một file logo và 2–3 mục việc làm. `passwordRef` là tên tham chiếu nội bộ để nhận mật khẩu lúc triển khai.

```yaml
dataset: campuswork-demo-v1
companies:
  - key: cw01
    account:
      fullName: Nguyễn Minh An
      email: cw01@campuswork-seed.example
      phone: "<số thử nghiệm riêng do nhóm quản lý>"
      passwordRef: cw01
      role: EMPLOYER
    profile:
      orgName: Cà phê Mây Sớm
      address: Quận 3, TP. Hồ Chí Minh
      industry: Ăn uống
      companySize: 10–20 người
      description: Quán cà phê giả lập với các ca linh hoạt cho sinh viên.
      tagline: Một ca làm, thêm một kỹ năng
      foundedYear: 2022
      benefits: Hướng dẫn tại chỗ | Hỗ trợ đổi ca
    logo:
      key: cw01-logo
      asset: logos/cw01.png
      contentType: image/png
    jobs:
      - key: cw01-j01
        title: Nhân viên pha chế ca tối
        salary: 32000
        jobType: PART_TIME
        payType: HOURLY
        # Các trường bắt buộc và thông tin ca lấy theo mẫu ở mục 4.
      - key: cw01-j02
        title: Nhân viên phục vụ cuối tuần
        salary: 30000
        jobType: PART_TIME
        payType: HOURLY
        # Bổ sung mô tả, lịch làm, địa điểm và deadline riêng.
```

Email có thể theo mẫu `cw01@campuswork-seed.example` đến `cw15@campuswork-seed.example` cho fixture nội bộ. Nếu kiểm thử OTP, thay bằng các địa chỉ thử nghiệm có thể nhận thư. Chọn 15 số đáp ứng định dạng và đã xác nhận thuộc bộ dữ liệu thử nghiệm. Không suy ra quyền sở hữu tài khoản từ một số điện thoại trùng.

| Mã | Công ty và ngành | Ví dụ tin và lương VNĐ/giờ | Logo dự kiến |
|---|---|---|---|
| cw01 | Cà phê Mây Sớm · Ăn uống | Pha chế ca tối 32.000, phục vụ cuối tuần 30.000 | Tách cà phê và mây |
| cw02 | Bếp Nhỏ Hẻm Xanh · Ăn uống | Phụ bếp 34.000, soạn đơn 30.000 | Nồi và lá xanh |
| cw03 | Hiệu sách Trang Mở · Bán lẻ | Sắp xếp sách 29.000, hỗ trợ quầy 31.000 | Sách mở |
| cw04 | Xưởng Nét Nâu · Thiết kế | Trợ lý thiết kế 48.000, chỉnh ảnh 45.000 | Nét cọ nâu |
| cw05 | Nhà hoa Nắng Mai · Bán lẻ | Gói hoa 35.000, chuẩn bị nguyên liệu 30.000 | Hoa và mặt trời |
| cw06 | Lớp học Cầu Giấy Nhỏ · Giáo dục | Trợ giảng 50.000, hỗ trợ học liệu 38.000, trực lớp 32.000 | Cây cầu và bút chì |
| cw07 | Tiệm bánh Hạt Bơ · Ăn uống | Phụ làm bánh 36.000, đóng gói bánh 30.000 | Bánh và hạt |
| cw08 | Phòng ảnh Khoảnh Khắc Nhỏ · Truyền thông | Hỗ trợ trường quay 42.000, phân loại ảnh 37.000 | Khung hình |
| cw09 | Cửa hàng Giấy Mộc · Bán lẻ | Tư vấn tại quầy 32.000, kiểm kê 31.000 | Cuộn giấy |
| cw10 | Nhóm Sự kiện Đốm Đèn · Sự kiện | Lễ tân 42.000, lắp đặt gian hàng 39.000, điều phối khách 44.000 | Đèn sân khấu |
| cw11 | Đồ thủ công Gốm Ven Sông · Thủ công | Trợ giảng làm gốm 41.000, chuẩn bị vật liệu 33.000 | Bình gốm và sóng |
| cw12 | Vườn Rau Hạt Mầm · Bán lẻ | Sơ chế rau 30.000, soạn giỏ hàng 32.000 | Hai lá mầm |
| cw13 | Đội nội dung Từ Hạt Mực · Truyền thông | Biên tập nội dung 46.000, nhập phụ đề 40.000 | Giọt mực |
| cw14 | Không gian học Bàn Chung · Dịch vụ | Trực quầy 33.000, chuẩn bị phòng 31.000 | Bàn học |
| cw15 | Kho Nhỏ Chuyển Tay · Vận hành | Đóng gói 34.000, kiểm hàng 36.000, cập nhật tồn kho 38.000 | Hộp carton |

Danh mục có 12 công ty × 2 tin và 3 công ty × 3 tin, tổng cộng 33 tin, 15 tài khoản, 15 hồ sơ và 15 logo. Tên, lương và hướng thiết kế chỉ là dữ liệu minh họa cho manifest.

## 6. Thiết kế chạy lại với thao tác chỉ thêm mới

1. **Kiểm tra đầu vào trước khi ghi.** Xác thực UTF-8, độ dài, enum, ngày, MIME, kích thước và 15 logo khác nội dung. Kiểm tra trùng khóa trong manifest và xung đột với DB. Dùng nhất quán email chuẩn hóa cho định danh tài khoản. Hai UNIQUE của `users` bảo vệ cả email và số điện thoại, còn tên công ty không đủ để nhận diện.
2. **Ghi nhận quyền sở hữu dữ liệu seed.** Dùng định danh ổn định như `cw01-j01` và `cw01-logo`. Đề xuất lưu ánh xạ `(dataset, loại bản ghi, key) → ID/ref` trong bảng quản lý riêng đã được phê duyệt cho môi trường seed, với UNIQUE trên bộ ba và ghi cùng transaction với bản ghi đích. Bảng này là đề xuất, chưa có trong schema. Nếu triển khai phải giữ nguyên schema, thay bằng khóa chạy độc quyền và quy ước định danh ổn định có thể tra lại, ví dụ email dành riêng và tiêu đề tin có hậu tố seed trong phạm vi một employer. Cách dùng tiêu đề kém bền nếu người dùng sửa tiêu đề. Khi không xác định chắc quyền sở hữu, báo xung đột và bỏ qua. Chỉ dùng truy vấn kiểm tra tồn tại rồi INSERT thì vẫn có thể trùng khi hai tiến trình chạy đồng thời.
3. **Ghi theo thứ tự và giữ nguyên bản ghi cũ.** Tạo tài khoản để nhận ID, chuẩn bị logo và metadata thuộc ID đó, chèn hồ sơ mới đã có `logo_ref`, rồi chèn các tin mới. Nếu dùng API đăng ký/upload thì chỉ điền hồ sơ cho tài khoản vừa tạo trong chính lần chạy. Gặp profile đã có từ trước dù thiếu logo, giữ nguyên và báo thiếu. Nếu mã đã ghi nhận, giữ nguyên mật khẩu, hồ sơ, trạng thái và deadline. Khi mã ánh xạ bị đứt hoặc email/điện thoại thuộc bản ghi khác, báo xung đột. Tránh `REPLACE`, upsert cập nhật, thao tác xóa, và `INSERT IGNORE` có thể che lỗi khác ngoài trùng khóa.
4. **Xử lý transaction và file.** Chọn một transaction theo công ty và ghi khóa sở hữu ngay trong transaction. Dùng tên file theo ref riêng và thao tác tạo mới độc quyền để tránh ghi đè. Chỉ dọn file mà lần chạy hiện tại vừa tạo nếu DB rollback. Vì filesystem và MySQL không commit cùng nhau, khi thử lại sau lỗi cần kiểm tra cả ánh xạ, metadata và file. Báo các file treo hoặc thiếu để đối chiếu, giữ nguyên file và bản ghi đã có từ trước.
5. **Nghiệm thu trên DB thử nghiệm.** Lần đầu trên DB sạch của bộ dữ liệu phải thêm đúng 15 users, 15 profiles, 15 stored files và 33 jobs. Lần hai thêm 0, cập nhật 0, xóa 0. So sánh dữ liệu đã có trước mỗi lần chạy. Kiểm tra từng logo trả ảnh, từng user có thể đăng nhập với mật khẩu tương ứng, và cả 33 tin hiển thị với hậu tố lương theo giờ khi còn hạn. Thử xung đột email, trùng số điện thoại, chạy đồng thời và lỗi ghi file giữa chừng. Nhật ký chỉ ghi mã seed, ID/ref và lý do kỹ thuật cần thiết.

`DemoDataInitializer` hiện có chỉ chạy theo profile phù hợp ngoài Render, thoát khi đã có user và dùng cùng một mật khẩu cấu hình cho tài khoản demo. Vì vậy nó không đáp ứng yêu cầu bổ sung 15 tài khoản vào DB đã có dữ liệu, mỗi tài khoản một mật khẩu và chạy lại từng mục. Đây là tham chiếu về dịch vụ và dữ liệu mẫu, chưa có thay đổi nào đối với initializer.

Nguồn tham chiếu · [DemoDataInitializer](../src/main/java/org/example/config/DemoDataInitializer.java), [schema.sql](../db/mysql/schema.sql), [FileStorageService](../src/main/java/org/example/service/FileStorageService.java).
