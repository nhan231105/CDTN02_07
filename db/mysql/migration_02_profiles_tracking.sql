-- Chạy MỘT LẦN trong MySQL Workbench trên DB freelance_marketplace đã có sẵn.
USE freelance_marketplace;

ALTER TABLE student_profiles
  ADD COLUMN school VARCHAR(150),
  ADD COLUMN major VARCHAR(150),
  ADD COLUMN study_year INT,
  ADD COLUMN skills VARCHAR(500),
  ADD COLUMN available_shifts VARCHAR(100),
  ADD COLUMN preferred_area VARCHAR(100);

ALTER TABLE employer_profiles
  ADD COLUMN website VARCHAR(255),
  ADD COLUMN industry VARCHAR(100),
  ADD COLUMN company_size VARCHAR(50);

ALTER TABLE applications
  MODIFY COLUMN status ENUM('PENDING','APPROVED','REJECTED','WITHDRAWN') NOT NULL,
  ADD COLUMN viewed_at DATETIME(6),
  ADD COLUMN withdrawn_at DATETIME(6);

-- Dữ liệu hồ sơ mẫu cho tài khoản demo
UPDATE student_profiles sp JOIN users u ON u.id = sp.user_id
SET sp.school = 'Đại học Kinh tế TP.HCM', sp.major = 'Quản trị kinh doanh', sp.study_year = 3,
    sp.skills = 'Giao tiếp, Excel, Tiếng Anh cơ bản', sp.available_shifts = 'EVENING,WEEKEND', sp.preferred_area = 'TP.HCM'
WHERE u.email = 'student.a@demo.local';

UPDATE employer_profiles ep JOIN users u ON u.id = ep.user_id
SET ep.website = 'https://example.com', ep.industry = 'Dịch vụ - Bán lẻ', ep.company_size = '10-50 nhân viên'
WHERE u.email IN ('employer.a@demo.local','employer.b@demo.local');
