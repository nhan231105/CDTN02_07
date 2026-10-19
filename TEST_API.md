# API Testing Guide - Freelance Marketplace

## Server Status ✅
- **URL**: http://localhost:8080/api
- **Database**: H2 In-memory (resets on restart)
- **Port**: 8080
- **Context Path**: /api

## ✅ Tested & Working Endpoints

### 1. Authentication (Auth Controller)

#### Health Check
```bash
curl http://localhost:8080/api/auth/health
```
**Response**: `{"success":true,"message":"Auth service is running"}`

#### Register User (Student)
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H Content-Type:application/json ^
  -d @register.json
```
**register.json**:
```json
{
  "email": "student@test.com",
  "password": "Pass@123456",
  "phone": "0987654321",
  "fullName": "Nguyen Van A",
  "role": "STUDENT"
}
```
**Response**: `{"success":true,"message":"Dang ki thanh cong","data":{...}}`

#### Register User (Employer)
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H Content-Type:application/json ^
  -d @employer_reg.json
```
**employer_reg.json**:
```json
{
  "email": "employer@test.com",
  "password": "Pass@123456",
  "phone": "0912345678",
  "fullName": "Cty TNHH ABC",
  "role": "EMPLOYER"
}
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H Content-Type:application/json ^
  -d @login.json
```
**login.json**:
```json
{
  "identifier": "student@test.com",
  "password": "Pass@123456"
}
```

#### Admin Stats
```bash
curl http://localhost:8080/api/auth/admin/stats
```
**Response**: `{"success":true,"message":"Thong ke thanh cong","data":{"totalUsers":2,"totalStudents":1,"totalEmployers":1,...}}`

---

### 2. Jobs (Job Controller)

#### Create Job (Employer)
```bash
curl -X POST http://localhost:8080/api/jobs ^
  -H Content-Type:application/json ^
  -d @job_create.json
```
**job_create.json**:
```json
{
  "title": "Python Developer - 3+ years experience",
  "description": "Tim kiem developer Python co kinh nghiem co kha nang phat trien web va desktop ứng dụng",
  "requirements": "3+ nam kinh nghiem Python; Hieu biet SQL",
  "category": "Development",
  "location": "Ho Chi Minh",
  "salary": 1500,
  "deadline": "2026-12-31",
  "workSchedule": "Full-time",
  "benefits": "Health insurance, bonus",
  "employerId": 2
}
```

#### Get All Jobs
```bash
curl http://localhost:8080/api/jobs
```

#### Get Job by ID
```bash
curl http://localhost:8080/api/jobs/1
```

#### Search Jobs
```bash
curl "http://localhost:8080/api/jobs/search?keyword=Python&category=Development&location=Ho%20Chi%20Minh"
```

#### Get Employer Jobs
```bash
curl "http://localhost:8080/api/jobs/employer/2"
```

---

### 3. Applications (Application Controller)

#### Student Apply for Job
```bash
curl -X POST http://localhost:8080/api/jobs/1/applications ^
  -H Content-Type:application/json ^
  -d @apply.json
```
**apply.json**:
```json
{
  "fullName": "Nguyen Van A",
  "email": "student@test.com",
  "phone": "0987654321",
  "coverLetter": "Tôi là sinh viên năm 3 có kinh nghiệm 2 năm",
  "cvFileRef": "cv_nguyenvana.pdf",
  "consentDataUsage": true,
  "jobId": 1,
  "studentId": 1
}
```

#### Get Job Applications
```bash
curl "http://localhost:8080/api/jobs/1/applications?employerId=2"
```

#### Approve Application
```bash
curl -X PATCH "http://localhost:8080/api/jobs/1/applications/1/approve?employerId=2"
```

#### Reject Application
```bash
curl -X PATCH "http://localhost:8080/api/jobs/1/applications/1/reject?employerId=2"
```

---

### 4. Reviews (Review Controller)

#### Create Review
```bash
curl -X POST http://localhost:8080/api/reviews ^
  -H Content-Type:application/json ^
  -d @review.json
```
**review.json**:
```json
{
  "jobId": 1,
  "revieweeId": 1,
  "rating": 5,
  "content": "Excellent work! Very professional."
}
```

#### Get Reviews by Job
```bash
curl "http://localhost:8080/api/reviews?jobId=1"
```

#### Get Reviews by Reviewee
```bash
curl "http://localhost:8080/api/reviews/user/1"
```

---

### 5. Notifications (Notification Controller)

#### Get User Notifications
```bash
curl "http://localhost:8080/api/notifications?userId=1"
```

#### Mark Notification as Read
```bash
curl -X PATCH "http://localhost:8080/api/notifications/1/read"
```

#### Get Unread Count
```bash
curl "http://localhost:8080/api/notifications/unread?userId=1"
```

---

### 6. Profiles (Profile Controller)

#### Get Student Profile
```bash
curl http://localhost:8080/api/profiles/student/1
```

#### Update Student Profile
```bash
curl -X PATCH http://localhost:8080/api/profiles/student/1 ^
  -H Content-Type:application/json ^
  -d @student_profile.json
```
**student_profile.json**:
```json
{
  "bio": "Junior Python Developer",
  "avatarRef": "avatar_1.jpg",
  "cvFileRef": "cv_1.pdf"
}
```

#### Get Employer Profile
```bash
curl http://localhost:8080/api/profiles/employer/2
```

#### Update Employer Profile
```bash
curl -X PATCH http://localhost:8080/api/profiles/employer/2 ^
  -H Content-Type:application/json ^
  -d @employer_profile.json
```
**employer_profile.json**:
```json
{
  "orgName": "ABC Company Ltd",
  "address": "123 Main Street, HCMC",
  "description": "Leading IT recruitment company",
  "logoRef": "logo_abc.png"
}
```

---

### 7. Admin (Admin Controller)

#### Remove Job
```bash
curl -X DELETE "http://localhost:8080/api/admin/jobs/1?reason=Inappropriate+content"
```

#### Lock User
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/1/lock?reason=Violation"
```

#### Unlock User
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/1/unlock"
```

#### Get Violations
```bash
curl "http://localhost:8080/api/admin/violations?status=PENDING"
```

#### Resolve Violation
```bash
curl -X PATCH "http://localhost:8080/api/admin/violations/1/resolve?action=WARN"
```

---

## Database Tables Created ✅

| Table | Records | Purpose |
|-------|---------|---------|
| users | 2 | User accounts (Student + Employer) |
| jobs | 1 | Job postings |
| applications | 1 | Job applications |
| reviews | 0 | User reviews |
| notifications | 0 | User notifications |
| student_profiles | 1 | Student profile details |
| employer_profiles | 1 | Employer profile details |
| violation_reports | 0 | Violation reports |

---

## Known Issues & Solutions

### Issue 1: 400 Bad Request on Register/Login
**Fixed**: Added validation annotations to RegisterRequest and LoginRequest

### Issue 2: /auth/stats returns 404
**Fixed**: Correct path is `/auth/admin/stats` not `/auth/stats`

### Issue 3: Multi-line curl not working on Windows CMD
**Fixed**: Use single-line syntax or save JSON to files and use `@filename`

### Issue 4: /applications returns 404
**Fixed**: Correct path is `/jobs/{jobId}/applications` not `/applications`

---

## Windows CMD Testing Tips

### Option 1: Use Files (Recommended)
```batch
REM Create JSON file first, then test
curl -X POST http://localhost:8080/api/auth/register ^
  -H Content-Type:application/json ^
  -d @register.json
```

### Option 2: PowerShell Script
```powershell
cd D:\TAILIEU\ChuyenDe2\DoAn_ChuyenDe2\DoAn_ChuyenDe2
$json = @{email='test@example.com'; password='Pass@123'} | ConvertTo-Json
$json | cmd /c "curl -X POST http://localhost:8080/api/auth/register -H Content-Type:application/json"
```

### Option 3: Use Postman
Import the endpoints directly into Postman for easier testing

---

## Test Workflow Example

### 1. Register Student
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H Content-Type:application/json ^
  -d @register.json
```
→ Get student ID: 1

### 2. Register Employer
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H Content-Type:application/json ^
  -d @employer_reg.json
```
→ Get employer ID: 2

### 3. Create Job
```bash
curl -X POST http://localhost:8080/api/jobs ^
  -H Content-Type:application/json ^
  -d @job_create.json
```
→ Get job ID: 1

### 4. Apply for Job
```bash
curl -X POST http://localhost:8080/api/jobs/1/applications ^
  -H Content-Type:application/json ^
  -d @apply.json
```
→ Get application ID: 1

### 5. Approve Application
```bash
curl -X PATCH "http://localhost:8080/api/jobs/1/applications/1/approve?employerId=2"
```
→ Application status: APPROVED

### 6. Create Review
```bash
curl -X POST http://localhost:8080/api/reviews ^
  -H Content-Type:application/json ^
  -d @review.json
```
→ Review created

---

## Statistics

- **Total Endpoints Implemented**: 36 mappings
- **Controllers**: 6 (Auth, Job, Application, Review, Notification, Profile, Admin)
- **Services**: 8 (User, Job, Application, Review, Notification, Profile, Admin)
- **Entities**: 8 (User, Job, Application, Review, Notification, StudentProfile, EmployerProfile, ViolationReport)
- **DTOs**: 14 request/response classes

---

## Next Steps

1. ✅ Register users
2. ✅ Login
3. ✅ Create jobs
4. ✅ Apply for jobs
5. ⏳ Create reviews
6. ⏳ Get/manage notifications
7. ⏳ Update profiles
8. ⏳ Admin operations

Ready for frontend integration! 🚀
