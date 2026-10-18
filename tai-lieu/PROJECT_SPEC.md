# PROJECT SPEC - Freelance / Part-time Job Marketplace for Students

> Context file for GitHub Copilot. Generate code that follows this spec exactly.
> Stack: Java 21, Spring Boot (REST), Spring Security + JWT, Spring Data JPA/Hibernate, MySQL. Frontend: ReactJS (separate).
> Architecture: modular monolith, layered: Controller (DTO + validation) -> Service -> Repository -> Entity.
> Never put SQL/business logic in Controller. Never return Entity directly, always use DTO.
> Passwords hashed with BCrypt. Every protected API checks JWT + role + resource ownership.
> Return 401 when token missing/invalid, 403 when role/ownership wrong, 409 on duplicate, 400 on validation error.

## Package structure

```
com.example.marketplace
  config/  security/  common/
  auth/  user/  profile/  job/  application/  review/  notification/  admin/
  (each module: controller/ dto/ service/ repository/ entity/)
```

## Roles (Actor)

- STUDENT (Sinh vien)
- EMPLOYER (Nha tuyen dung)
- ADMIN (Quan tri vien)

## Business flow

post job -> student applies -> employer approves/rejects -> job completed -> two-way review

---

## DATABASE SCHEMA (MySQL)

Legend: PK = primary key, FK = foreign key, UQ = unique.

### users
| column | type | notes |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | PK |
| full_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UQ |
| phone | VARCHAR(20) | NOT NULL, UQ |
| password_hash | VARCHAR(255) | BCrypt |
| role | ENUM('STUDENT','EMPLOYER','ADMIN') | NOT NULL |
| status | ENUM('ACTIVE','LOCKED') | default ACTIVE |
| lock_reason | VARCHAR(500) | nullable |
| contact_changed_at | DATETIME | nullable, limits changing phone/email/password (48h rule) |
| created_at, updated_at | DATETIME | |

### student_profiles
| column | type | notes |
|---|---|---|
| user_id | BIGINT | PK, FK -> users.id (1-1) |
| avatar_ref | VARCHAR(255) | nullable |
| bio | TEXT | self introduction |
| cv_file_ref | VARCHAR(255) | nullable, PDF <= 5MB, stored in private folder |
| cv_original_name | VARCHAR(255) | nullable |

### employer_profiles
| column | type | notes |
|---|---|---|
| user_id | BIGINT | PK, FK -> users.id (1-1) |
| org_name | VARCHAR(150) | |
| address | VARCHAR(255) | |
| description | TEXT | company description |
| logo_ref | VARCHAR(255) | nullable |

### jobs
| column | type | notes |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | PK |
| employer_id | BIGINT | FK -> users.id (job owner) |
| title | VARCHAR(200) | |
| category | VARCHAR(100) | industry/ngành nghề |
| description | TEXT | |
| requirements | TEXT | |
| benefits | TEXT | |
| salary | DECIMAL(12,0) | numeric, used for filtering |
| location | VARCHAR(100) | city |
| work_schedule | VARCHAR(200) | |
| deadline | DATE | application deadline |
| status | ENUM('OPEN','FILLED','COMPLETED') | default OPEN |
| removed_by_admin | BOOLEAN | default false |
| removal_reason | VARCHAR(500) | nullable |
| created_at, updated_at | DATETIME | |

Index: (status, location, category)

### applications
| column | type | notes |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | PK |
| job_id | BIGINT | FK -> jobs.id |
| student_id | BIGINT | FK -> users.id |
| cv_file_ref | VARCHAR(255) | CV used for this application |
| full_name, email, phone | VARCHAR | contact info on the form |
| cover_letter | TEXT | |
| consent_data_usage | BOOLEAN | employer may use student's personal data |
| status | ENUM('PENDING','APPROVED','REJECTED') | default PENDING |
| created_at | DATETIME | |
| decided_at | DATETIME | nullable |

Constraint: UQ (job_id, student_id) - no duplicate application.

### reviews
| column | type | notes |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | PK |
| job_id | BIGINT | FK -> jobs.id |
| reviewer_id | BIGINT | FK -> users.id |
| reviewee_id | BIGINT | FK -> users.id |
| rating | TINYINT | 1..5 |
| content | TEXT | |
| is_hidden | BOOLEAN | default false (admin moderation, optional) |
| created_at | DATETIME | |

Constraint: UQ (job_id, reviewer_id, reviewee_id) - one review per side per job.

### notifications
| column | type | notes |
|---|---|---|
| id | BIGINT AUTO_INCREMENT | PK |
| user_id | BIGINT | FK -> users.id (recipient) |
| message | VARCHAR(500) | |
| status | ENUM('UNREAD','READ') | default UNREAD |
| related_type | VARCHAR(50) | nullable |
| related_id | BIGINT | nullable |
| created_at | DATETIME | |

### violation_reports (OPTIONAL - only if FR-15 is kept)
| column | type | notes |
|---|---|---|
| id | BIGINT | PK |
| reporter_id | BIGINT | FK -> users.id |
| target_type | VARCHAR(20) | USER / JOB |
| target_id | BIGINT | |
| reason | VARCHAR(500) | |
| status | ENUM('PENDING','RESOLVED','REJECTED') | |
| admin_action | VARCHAR(100) | warn / lock account |
| created_at | DATETIME | |

### Relationships

```
users 1-1 student_profiles
users 1-1 employer_profiles
users(EMPLOYER) 1-N jobs
jobs 1-N applications N-1 users(STUDENT)
jobs 1-N reviews (reviewer_id, reviewee_id -> users)
users 1-N notifications
```

---

## STATE RULES

| entity | states | rules |
|---|---|---|
| Job | OPEN -> FILLED -> COMPLETED | only owner changes status; only OPEN accepts applications |
| Application | PENDING -> APPROVED or REJECTED | only the job owner decides; block double decision |
| Review | created only when Job is COMPLETED | reviewer must belong to the job; unique per (job, reviewer, reviewee) |
| Notification | UNREAD -> READ | only the owner can read/mark |
| User | ACTIVE <-> LOCKED | LOCKED user cannot use protected APIs (check status on each request or short JWT TTL) |

Validation rules:
- Password: min 8 chars, has uppercase, lowercase, special char.
- Email: valid format. Duplicate email -> 409 "Email nay da duoc dang ki".
- Wrong login -> "Ten dang nhap hoac mat khau chua dung".
- CV: PDF only, max 5MB, check MIME, store outside static/public folder.
- Job list: max 12 jobs per page, only non-removed jobs.

---

## API LIST

Priority: MUST = core MVP, SHOULD = after MVP, COULD = skip unless time.

### Auth (MUST)
| method | endpoint | role | description |
|---|---|---|---|
| POST | /api/auth/register | Public | Register (full_name, phone, email, password, role = STUDENT or EMPLOYER) |
| POST | /api/auth/login | Public | Login by email or phone + password, return JWT + role. Reject LOCKED accounts |

### Profile (MUST)
| method | endpoint | role | description |
|---|---|---|---|
| GET | /api/me/profile | STUDENT, EMPLOYER | Get own profile |
| PATCH | /api/me/profile | STUDENT, EMPLOYER | Student: avatar, bio. Employer: org name, address, description, phone |
| POST | /api/me/cv | STUDENT | Upload CV (PDF, <= 5MB) |
| PATCH | /api/me/account | STUDENT, EMPLOYER | Change phone/email/password (48h limit) |

### Job (MUST)
| method | endpoint | role | description |
|---|---|---|---|
| POST | /api/jobs | EMPLOYER | Create job, default status OPEN |
| GET | /api/jobs?keyword=&category=&location=&time=&salary=&page= | Public | Search + filter + paginate (12/page). Return title, employer name, salary, city |
| GET | /api/jobs/{id} | Public | Job detail |
| GET | /api/me/jobs | EMPLOYER | Own jobs |
| PATCH | /api/jobs/{id} | EMPLOYER (owner) | Edit job (only if nobody applied yet) |
| PATCH | /api/jobs/{id}/status | EMPLOYER (owner) | OPEN -> FILLED -> COMPLETED; locks applying when not OPEN |

### Application (MUST)
| method | endpoint | role | description |
|---|---|---|---|
| POST | /api/jobs/{id}/applications | STUDENT | Apply. Check: job OPEN, valid CV, no duplicate. Create PENDING, return 201 |
| GET | /api/jobs/{id}/applications | EMPLOYER (owner) | List applicants: name, submitted time, CV, cover letter, status |
| GET | /api/me/applications | STUDENT | Track own applications |
| GET | /api/students/{id}/profile | EMPLOYER (related) | View applicant profile (only applicants of own jobs) |
| PATCH | /api/applications/{id}/decision | EMPLOYER (owner) | APPROVED or REJECTED; create Notification in same transaction |
| GET | /api/applications/{id}/cv | STUDENT (owner), EMPLOYER (owner) | Download CV via authorized API |

### Review (SHOULD)
| method | endpoint | role | description |
|---|---|---|---|
| POST | /api/jobs/{id}/reviews | STUDENT, EMPLOYER | Create review (rating 1-5 + content). Only when job COMPLETED. Student reviews employer, employer reviews approved student |
| GET | /api/jobs/{id}/reviews | Authenticated | List reviews of a job |
| GET | /api/users/{id}/reviews | Public | Public reviews on a profile |

### Notification (SHOULD)
| method | endpoint | role | description |
|---|---|---|---|
| GET | /api/me/notifications | Authenticated | List notifications (unread first). Use polling, no WebSocket |
| PATCH | /api/me/notifications/{id} | owner | Mark as READ |

### Admin (SHOULD)
| method | endpoint | role | description |
|---|---|---|---|
| GET | /api/admin/users | ADMIN | List/filter by role, status, search name/email |
| PATCH | /api/admin/users/{id} | ADMIN | Lock/unlock with reason |
| DELETE | /api/admin/users/{id} | ADMIN | Delete account |
| GET | /api/admin/jobs | ADMIN | List/filter/paginate jobs (12/page) |
| PATCH | /api/admin/jobs/{id} | ADMIN | Remove job, reason required, notify employer |
| GET | /api/admin/reports | ADMIN | (only if FR-15 kept) |
| PATCH | /api/admin/reports/{id} | ADMIN | (only if FR-15 kept) |
| GET | /api/admin/stats | ADMIN | (COULD) totals of students/employers/jobs/applications |
| PATCH | /api/admin/reviews/{id} | ADMIN | (COULD) hide review |

### Hello World / health check
| method | endpoint | description |
|---|---|---|
| GET | /api/health | 200 + `{"status":"OK","service":"freelance-marketplace"}`; must be permitAll in Spring Security |

---

## NON-FUNCTIONAL REQUIREMENTS

- NFR-01: BCrypt password hashing, strong password validation.
- NFR-02: all APIs check JWT + role + ownership.
- NFR-03: CV PDF only, <= 5MB.
- NFR-04: job list loads <= 3s, 12 jobs/page, add DB index.
- NFR-05: responsive UI from 360px (frontend).
- Do not commit secrets (.env, JWT secret, DB password, real CV files). Use application.yml with environment variables.

## OPEN QUESTIONS (not decided yet, do not hardcode assumptions)

- When does a job auto-switch to FILLED? One or many approved applicants per job?
- Who confirms COMPLETED?
- Employer cancel / repost job?
- Whether to keep violation_reports (FR-15).
- Use case doc says job states "Open / In Progress / Completed" but SRS says OPEN / FILLED / COMPLETED. This file follows SRS.
