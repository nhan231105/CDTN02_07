# Tra cứu nhanh: Chức năng ↔ Trang Frontend ↔ API Backend ↔ File Code

> Mục đích: dùng để bảo vệ đồ án — khi được hỏi "chức năng X nằm ở đâu", tra bảng bên dưới
> để trả lời ngay: trang nào, gọi API nào, controller/service nào xử lý.

## 0. Kiến trúc tổng quan

- **Backend**: Spring Boot (Java), package gốc `src/main/java/org/example`
  - `controller/` – nhận HTTP request (REST API)
  - `service/` – xử lý logic nghiệp vụ
  - `repository/` – truy vấn DB (Spring Data JPA)
  - `entity/` – các bảng DB ánh xạ Java object
  - `dto/` – dữ liệu trả về / nhận vào qua API
  - `config/SecurityConfig.java` – phân quyền theo vai trò (STUDENT/EMPLOYER/ADMIN)
  - `security/` – JWT (đăng nhập, xác thực token)
- **Frontend**: React (Vite), thư mục `freelance-frontend/src`
  - `pages/` – mỗi file là 1 trang (route)
  - `api/apiService.js` – nơi DUY NHẤT gọi API (mọi page đều import `api` từ đây)
  - `components/` – UI dùng chung (Modal, Pagination, Status, Layout...)
  - `context/auth.jsx` – quản lý trạng thái đăng nhập, lưu token
  - `App.jsx` – khai báo route (URL ↔ page), phân quyền theo `RequireAuth roles=[...]`
  - `App.css` – toàn bộ style

**Luồng chuẩn của 1 chức năng**:
`Trang .jsx` → gọi hàm trong `apiService.js` → `fetch` tới `/api/...`
→ `XController.java` nhận request → gọi `XService(Impl).java` xử lý logic
→ `XRepository.java` truy vấn DB (bảng trong `db/mysql/schema.sql`) → trả `XDTO` về frontend.

---

## 1. Xác thực & phân quyền (Đăng ký / Đăng nhập)

| Chức năng | Trang (Frontend) | API gọi (apiService.js) | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| Đăng ký tài khoản | `pages/Register.jsx` | `api.register(data)` | `POST /auth/register` | `AuthController.java` | `AuthServiceImpl` (trong `service/`) |
| Đăng nhập | `pages/Login.jsx` | `api.login(data)` | `POST /auth/login` | `AuthController.java` | `AuthServiceImpl` |
| Lưu phiên đăng nhập / token | `context/auth.jsx` | — | — | `security/JwtAuthenticationFilter.java` | — |
| Phân quyền theo vai trò | `App.jsx` (`<RequireAuth roles={[...]}/>`) | — | — | `config/SecurityConfig.java` (chặn API theo role ở backend) | — |

**Route FE**: `/login`, `/register`
**Vai trò (role)**: `STUDENT`, `EMPLOYER`, `ADMIN` — lưu ở bảng `users.role` (schema.sql).

---

## 2. Sinh viên (STUDENT)

| Chức năng | Trang | Hàm API | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| Xem/tìm/lọc danh sách việc làm | `pages/Jobs.jsx` | `api.jobs(params)` | `GET /jobs` hoặc `/jobs/search` | `JobController.list()` | `JobServiceImpl.searchJobs()` |
| Xem chi tiết 1 việc làm | `pages/JobDetail.jsx` | `api.job(id)` | `GET /jobs/{id}` | `JobController.get()` | `JobServiceImpl.getJobById()` |
| Ứng tuyển việc làm | `pages/JobDetail.jsx` (modal ứng tuyển) | `api.apply(id, data)` | `POST /jobs/{id}/applications` | `ApplicationController.java` | `ApplicationServiceImpl` |
| Xem đơn ứng tuyển của mình | `pages/Applications.jsx` | `api.applications(page)` | `GET /me/applications` | `StudentApplicationController.java` | — |
| Cập nhật hồ sơ sinh viên + Upload CV/avatar | `pages/StudentProfile.jsx` | `api.profile()`, `api.saveProfile()`, `upload('/me/cv', file)` | `GET/PUT /me/profile`, `POST /me/cv`, `POST /me/avatar` | `MeController.java`, `ProfileController.java` | `ProfileService` / `FileService` |
| Đánh giá nhà tuyển dụng sau khi hoàn thành việc | `pages/Applications.jsx` (form review) | `api.review(id, data)` | `POST /applications/{id}/reviews` | `StudentApplicationController.java` | `ReviewServiceImpl` |
| Xem thông báo | `pages/Notifications.jsx` | `api.notifications(page)`, `api.unread()`, `api.read(id)` | `GET /me/notifications`, `GET /me/notifications/count/unread`, `PATCH /me/notifications/{id}/read` | `NotificationController.java` | `NotificationServiceImpl` |
| **Báo cáo tin tuyển dụng vi phạm (UC_10)** | `pages/JobDetail.jsx` (nút "⚑ Báo cáo tin tuyển dụng") | `api.submitReport({targetType:'JOB',...})` | `POST /reports` | `ReportController.java` | `ReportServiceImpl.java` |

**Route FE**: `/jobs`, `/jobs/:id`, `/student/profile`, `/student/applications`, `/notifications`

---

## 3. Nhà tuyển dụng (EMPLOYER)

| Chức năng | Trang | Hàm API | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| Đăng tin tuyển dụng mới | `pages/EmployerDashboard.jsx` | `api.createJob(data)` | `POST /jobs` | `JobController.create()` | `JobServiceImpl.createJob()` |
| Xem/Sửa tin của mình | `pages/EmployerDashboard.jsx` | `api.employerJobs(id,page)`, `api.updateJob(id,data)` | `GET /jobs/employer/{id}`, `PUT /jobs/{id}` | `JobController.own()/update()` | `JobServiceImpl` |
| Đổi trạng thái tin (mở/đóng/hoàn thành) | `pages/EmployerDashboard.jsx` | `api.status(id,status)` | `PATCH /jobs/{id}/status` | `JobController.status()` | `JobServiceImpl.updateJobStatus()` |
| Xem danh sách ứng viên của 1 tin | `pages/Applicants.jsx` | `api.applicants(id,page)` | `GET /jobs/{id}/applications` | `ApplicationController.java` | `ApplicationServiceImpl` |
| Duyệt / Từ chối ứng viên | `pages/Applicants.jsx` | `api.decide(id,decision)` | `PATCH /applications/{id}/decision` | `ApplicationDecisionController.java` | `ApplicationServiceImpl` |
| Tải CV ứng viên | `pages/Applicants.jsx` | `download('/applications/{id}/cv')` | `GET /applications/{id}/cv` | `MeController.applicationCv()` | `FileService` |
| Cập nhật hồ sơ công ty | `pages/EmployerProfile.jsx` | `api.profile()/saveProfile()` qua `ProfileController` | `GET/PATCH /profiles/employers/{id}` | `ProfileController.java` | — |
| **Báo cáo ứng viên vi phạm (UC_10)** | `pages/Applicants.jsx` (nút "⚑ Báo cáo ứng viên") | `api.submitReport({targetType:'USER',...})` | `POST /reports` | `ReportController.java` | `ReportServiceImpl.java` |

**Route FE**: `/employer/jobs`, `/employer/jobs/:id/applications`, `/employer/profile`

---

## 4. Quản trị viên (ADMIN) — UC_08, UC_09, UC_10

> Toàn bộ nằm trong **1 trang duy nhất**: `pages/AdminDashboard.jsx` (route `/admin`), chia 3 tab:
> **Tài khoản** / **Tin tuyển dụng** / **Báo cáo vi phạm**.

### UC_08 – Quản lý tài khoản người dùng

| Chức năng | Vị trí UI (AdminDashboard.jsx) | Hàm API | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| Thống kê tổng quan (tổng user, SV, NTD, đang hoạt động, đã khóa) | Đầu trang (`stats-grid`) | `api.stats()` | `GET /auth/admin/stats` | `AuthController.java` | `AuthServiceImpl` |
| Tìm kiếm / lọc tài khoản (từ khóa, vai trò, trạng thái) | Tab "Tài khoản" → form lọc | `api.adminUsers(params)` | `GET /auth/admin/search` | `AuthController.java` | `AuthServiceImpl` |
| Khóa tài khoản (kèm lý do) | Tab "Tài khoản" → nút "Khóa tài khoản" + modal | `api.adminSetUserStatus(id,'LOCKED',reason)` | `PATCH /admin/users/{id}/status` | `AdminController.updateUserStatus()` | `AdminServiceImpl.lockUser()` |
| Mở khóa tài khoản | Tab "Tài khoản" → nút "Mở khóa" | `api.adminSetUserStatus(id,'ACTIVE')` | `PATCH /admin/users/{id}/status` | `AdminController.updateUserStatus()` | `AdminServiceImpl.unlockUser()` |
| **Xóa tài khoản** (chặn nếu còn dữ liệu liên quan) | Tab "Tài khoản" → nút "Xóa" (đỏ) + modal xác nhận | `api.adminDeleteUser(id)` | `DELETE /admin/users/{id}` | `AdminController.deleteUser()` | `AdminServiceImpl.deleteUser()` |

### UC_09 – Kiểm duyệt tin tuyển dụng

| Chức năng | Vị trí UI | Hàm API | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| Xem TOÀN BỘ tin tuyển dụng (kể cả đã gỡ/đóng) để kiểm duyệt | Tab "Tin tuyển dụng" | `api.adminAllJobs(page)` | `GET /admin/jobs` | `AdminController.getAllJobsForModeration()` | `AdminServiceImpl.getAllJobsForModeration()` |
| Gỡ tin vi phạm (kèm lý do) | Tab "Tin tuyển dụng" → nút "Gỡ tin" + modal | `api.adminRemoveJob(id,reason)` | `PATCH /admin/jobs/{id}/moderation` | `AdminController.moderateJob()` | `AdminServiceImpl.removeJob()` |
| Duyệt lại tin đã gỡ (khôi phục) | Tab "Tin tuyển dụng" → nút "Duyệt lại" (hiện khi tin đã bị gỡ) | `api.adminRestoreJob(id)` | `PATCH /admin/jobs/{id}/restore` | `AdminController.restoreJob()` | `AdminServiceImpl.restoreJob()` |

### UC_10 – Xử lý báo cáo vi phạm

| Chức năng | Vị trí UI | Hàm API | Endpoint | Controller | Service |
|---|---|---|---|---|---|
| SV/NTD gửi báo cáo vi phạm | `JobDetail.jsx` (báo cáo tin), `Applicants.jsx` (báo cáo ứng viên) | `api.submitReport(data)` | `POST /reports` | `ReportController.java` | `ReportServiceImpl.submitReport()` |
| Admin xem danh sách báo cáo đang chờ xử lý | Tab "Báo cáo vi phạm" | `api.adminReports(page)` | `GET /admin/reports` | `AdminController.getViolationReports()` | `AdminServiceImpl.getViolationReports()` |
| Admin xử lý báo cáo (ghi quyết định: cảnh cáo/khóa...) | Tab "Báo cáo vi phạm" → nút "Xử lý" + modal | `api.adminResolveReport(id, adminAction)` | `PATCH /admin/reports/{id}/resolve` | `AdminController.resolveViolation()` | `AdminServiceImpl.resolveViolation()` |
| Admin từ chối báo cáo (không xử lý) | Tab "Báo cáo vi phạm" → nút "Từ chối" | `api.adminRejectReport(id)` | `PATCH /admin/reports/{id}/reject` | `AdminController.rejectViolation()` | `AdminServiceImpl.rejectViolation()` |

**Route FE**: `/admin` (chỉ role `ADMIN` truy cập được, kiểm tra ở `App.jsx` + chặn lại ở `SecurityConfig.java` phía backend).

---

## 5. File quan trọng cần nhớ tên khi bị hỏi dồn

| Vai trò file | Đường dẫn |
|---|---|
| Khai báo toàn bộ route/URL của web | `freelance-frontend/src/App.jsx` |
| Nơi DUY NHẤT gọi API từ frontend | `freelance-frontend/src/api/apiService.js` |
| Trang quản trị (3 chức năng UC_08/09/10) | `freelance-frontend/src/pages/AdminDashboard.jsx` |
| Trang danh sách + bộ lọc việc làm | `freelance-frontend/src/pages/Jobs.jsx` |
| Trang chi tiết việc làm + nút báo cáo | `freelance-frontend/src/pages/JobDetail.jsx` |
| Trang NTD xem & xử lý ứng viên + nút báo cáo | `freelance-frontend/src/pages/Applicants.jsx` |
| Controller xử lý toàn bộ API /admin/** | `src/main/java/org/example/controller/AdminController.java` |
| Logic nghiệp vụ quản trị (khóa/xóa/gỡ tin/duyệt lại) | `src/main/java/org/example/service/AdminServiceImpl.java` |
| Controller + Service xử lý gửi báo cáo vi phạm | `controller/ReportController.java`, `service/ReportServiceImpl.java` |
| Logic tìm kiếm/lọc việc làm (keyword, lương, khu vực...) | `src/main/java/org/example/service/JobServiceImpl.java` (hàm `searchJobs`) |
| Phân quyền API theo vai trò (STUDENT/EMPLOYER/ADMIN) | `src/main/java/org/example/config/SecurityConfig.java` |
| Định nghĩa toàn bộ bảng DB (không đổi) | `db/mysql/schema.sql` |

---

## 6. Mẹo trả lời nhanh khi giám khảo hỏi

- *"Chức năng X nằm ở file nào?"* → Mở bảng mục 2/3/4 theo chức năng, đọc cột **Trang** (frontend) và **Controller/Service** (backend).
- *"Khi bấm nút Y thì chuyện gì xảy ra?"* → Đọc cột **Hàm API** (tên hàm trong `apiService.js`) → cột **Endpoint** → cột **Controller** → cột **Service** (đây chính là luồng request đi qua).
- *"Dữ liệu lưu ở bảng nào?"* → Mở `db/mysql/schema.sql`, tìm tên bảng tương ứng (vd: báo cáo vi phạm → bảng `violation_reports`; tài khoản → bảng `users`; tin tuyển dụng → bảng `jobs`).
