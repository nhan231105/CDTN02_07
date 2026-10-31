# API Contract triển khai 7 UC

Base local: `http://localhost:8082/api`. Đây là bản đồng bộ implementation, thay cho hợp đồng **đề xuất** được giữ nguyên tại `reference/API_Contract.md`. Bằng chứng HTTP thực tế ở `evidence/api-smoke.json`; những endpoint không nằm trong script được kiểm tra qua backend/E2E theo TEST_REPORT.

## Xác thực và dữ liệu chung

- JSON request/response, trừ file multipart và download nhị phân. API bảo vệ dùng `Authorization: Bearer <accessToken>`; subject là userID. Không gửi token qua query string.
- `POST auth/register/login` giữ wrapper `{success,data,message,timestamp}`. API nghiệp vụ trả DTO trực tiếp; danh sách dùng Spring Page `{content,totalElements,totalPages,number,size,...}`.
- `LoginResponse.data` gồm `id,fullName,email,phone,role,status,accessToken,tokenType,expiresAt`. Token một giờ, không refresh token; token sai/hết hạn→401, sai quyền→403.
- Lỗi: `{code,message,timestamp,fieldErrors?}`.400 input,401 xác thực,403 quyền,404 không tồn tại,409 trùng/trạng thái,413 quá size,415 sai định dạng. Không trả stacktrace.
- ID người thao tác lấy từ principal. Tham số ID của API legacy chỉ để tương thích; nếu gửi ID khác principal thì403.

## Auth / hồ sơ / file

| Method | Endpoint | Quyền | Request / response |
|---|---|---|---|
| POST | `/auth/register` | Public | `{fullName,email,phone,password,role}`; role STUDENT/EMPLOYER;201 wrapper user;400/409 |
| POST | `/auth/login` | Public | `{identifier,password}`; identifier=email hoặc SĐT;200 wrapper login;401/403 |
| GET | `/auth/health` | Public |200 trạng thái dịch vụ |
| GET | `/me/profile` | Auth |200 DTO phẳng account+profile theo role |
| PUT/PATCH | `/me/profile` | Auth | `{fullName?,bio?,orgName?,address?,description?}`;200 profile đã lưu |
| PATCH | `/me/account` | Auth | `{currentPassword,email?,phone?,password?}`;200 `{message,requiresLogin}`;409 chưa đủ48h/trùng; đổi mật khẩu thu hồi token cũ |
| POST | `/me/cv` | STUDENT | Multipart field `file`, PDF thật≤5MiB;201 `{ref,originalName,size,contentType}` |
| GET | `/me/cv` | STUDENT owner |200 PDF attachment, no-store |
| POST | `/me/avatar` | STUDENT | Multipart `file`, PNG/JPEG thật≤2MiB;201 metadata |
| GET | `/me/avatar` | STUDENT owner |200 ảnh inline, no-store |
| GET | `/applications/{id}/cv` | Student owner hoặc employer của job |200 PDF snapshot của đơn;403 quyền chéo |

Profile gồm `id,fullName,email,phone,role,status` và các trường theo vai trò: `avatarRef,bio,cvFileRef,cvOriginalName,cvSize` hoặc `orgName,address,description`. Upload dùng file thực; client không được tự chọn đường dẫn filesystem. Tải CV ứng tuyển là endpoint hỗ trợ được bổ sung vì contract ban đầu chưa mô tả đầy đủ quyền tải.

## Jobs và employer support

| Method | Endpoint | Quyền | Request / response |
|---|---|---|---|
| GET | `/jobs` | Public | Query `keyword,category,location,minSalary,maxSalary,workSchedule,page,size`; AND, tối đa12/page;200 Page<JobDTO> |
| GET | `/jobs/{id}` | Public cho tin đang mở |200 JobDTO; tin đóng chỉ owner/ứng viên liên quan được đọc theo quyền;404 nếu không công khai |
| POST | `/jobs` | EMPLOYER | `{title,description,requirements?,benefits?,category,location,salary,workSchedule,deadline}`;201 JobDTO OPEN |
| PUT/PATCH | `/jobs/{id}` | EMPLOYER owner |Các trường editable;200;409 nếu không còn điều kiện sửa |
| GET | `/jobs/employer/{employerId}` | EMPLOYER chính chủ |200 Page<JobDTO> |
| PATCH | `/jobs/{id}/status` | EMPLOYER owner |`{status: FILLED|COMPLETED}`; OPEN→FILLED cần APPROVED, rồi→COMPLETED;200/409 |
| GET | `/jobs/{id}/applications` | EMPLOYER owner |200 Page<ApplicationDTO> |
| PATCH | `/applications/{id}/decision` | EMPLOYER owner |`{decision: APPROVED|REJECTED}` (alias `status` hỗ trợ);200 DTO,409 đã xử lý/sai trạng thái |

JobDTO gồm `id,title,description,requirements,benefits,category,location,workSchedule,salary,deadline,status,employerId,employerName,createdAt,updatedAt`. Salary là số tiền VND. Danh sách public chỉ nonremoved,OPEN,chưa quá hạn; sort `createdAt DESC,id DESC` ổn định. Không tự FILLED lúc approve.

## Apply / review / notifications

| Method | Endpoint | Quyền | Request / response |
|---|---|---|---|
| POST | `/jobs/{id}/applications` | STUDENT |`{coverLetter,consentDataUsage:true,cvFileRef?}`; thông tin liên hệ tùy chọn lấy từ account;201 ApplicationDTO;409 trùng/đóng |
| GET | `/me/applications` | STUDENT |200 Page<ApplicationDTO>, đọc DB sau reload |
| POST | `/applications/{id}/reviews` | Student owner; chiều employer giữ tương thích |`{rating:1..5,content}`; APPROVED+COMPLETED;201 ReviewDTO;403/409 |
| GET | `/reviews/jobs/{jobId}` | Public |200 Page<ReviewDTO> không hidden |
| GET | `/reviews/users/{userId}` | Public |200 Page<ReviewDTO> không hidden |
| GET | `/me/notifications` | Auth |200 Page<NotificationDTO>, unread trước |
| GET | `/me/notifications/count/unread` | Auth |200 số nguyên |
| GET | `/me/notifications/{id}` | Owner |200 DTO;403 người khác |
| PATCH | `/me/notifications/{id}/read` | Owner |200 DTO READ, gọi lại vẫn READ |

ApplicationDTO gồm ID job/student, `jobTitle,jobStatus,employerId,employerName,reviewSubmitted`, contact/CV/coverLetter, `status,consentDataUsage,createdAt,decidedAt`. Reviewer/reviewee của canonical review được suy ra ở backend, không cho giả danh bằng payload.

Giữ alias `/notifications`, `/profiles/students/{id}`, `/profiles/employers/{id}`, `/reviews/jobs/{id}` và các quyết định cũ nếu source đã có, nhưng áp dụng JWT/role/ownership tương đương. Admin endpoints hiện hữu chỉ ADMIN và không thuộc tuyên bố hoàn thành toàn bộ FR-13…15.
