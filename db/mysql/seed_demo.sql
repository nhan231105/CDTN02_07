-- Optional NEW LOCAL DEV MySQL fixture, NOT executed by the agent.
-- All accounts use DemoPass123! (dev only); BCrypt generated and matches verified
-- with Spring Security 6.3.3. Never use these accounts/hash outside local demo.
-- Metadata for CV/avatar is intentionally omitted: upload physical files through API.
USE freelance_marketplace;
START TRANSACTION;
INSERT INTO users(full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at)
SELECT 'Student A','student.a@demo.local','0900000001','$2a$10$ewoYCIs74QiUaLuX4faKPeCOMyC0ju7.8hVMRr0WVBUSbiWxFQu.e','STUDENT','ACTIVE',0,NOW(6),NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='student.a@demo.local');
INSERT INTO student_profiles(user_id,bio)
SELECT id,'Local MySQL demo profile' FROM users u WHERE u.email='student.a@demo.local'
AND NOT EXISTS(SELECT 1 FROM student_profiles p WHERE p.user_id=u.id);
INSERT INTO users(full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at)
SELECT 'Student B','student.b@demo.local','0900000002','$2a$10$ewoYCIs74QiUaLuX4faKPeCOMyC0ju7.8hVMRr0WVBUSbiWxFQu.e','STUDENT','ACTIVE',0,NOW(6),NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='student.b@demo.local');
INSERT INTO student_profiles(user_id,bio)
SELECT id,'Local MySQL demo profile' FROM users u WHERE u.email='student.b@demo.local'
AND NOT EXISTS(SELECT 1 FROM student_profiles p WHERE p.user_id=u.id);
INSERT INTO users(full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at)
SELECT 'Employer A','employer.a@demo.local','0900000003','$2a$10$ewoYCIs74QiUaLuX4faKPeCOMyC0ju7.8hVMRr0WVBUSbiWxFQu.e','EMPLOYER','ACTIVE',0,NOW(6),NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='employer.a@demo.local');
INSERT INTO employer_profiles(user_id,org_name,address,description)
SELECT id,'Employer A Studio','Ho Chi Minh City','Local demo employer' FROM users u WHERE u.email='employer.a@demo.local'
AND NOT EXISTS(SELECT 1 FROM employer_profiles p WHERE p.user_id=u.id);
INSERT INTO users(full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at)
SELECT 'Employer B','employer.b@demo.local','0900000004','$2a$10$ewoYCIs74QiUaLuX4faKPeCOMyC0ju7.8hVMRr0WVBUSbiWxFQu.e','EMPLOYER','ACTIVE',0,NOW(6),NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='employer.b@demo.local');
INSERT INTO employer_profiles(user_id,org_name,address,description)
SELECT id,'Employer B Studio','Ho Chi Minh City','Local demo employer' FROM users u WHERE u.email='employer.b@demo.local'
AND NOT EXISTS(SELECT 1 FROM employer_profiles p WHERE p.user_id=u.id);
INSERT INTO users(full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at)
SELECT 'Demo Admin','admin@demo.local','0900000005','$2a$10$ewoYCIs74QiUaLuX4faKPeCOMyC0ju7.8hVMRr0WVBUSbiWxFQu.e','ADMIN','ACTIVE',0,NOW(6),NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='admin@demo.local');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 1','Development','Local development opportunity for students. Connect through real REST API.',110000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 1');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 2','Design','Local development opportunity for students. Connect through real REST API.',120000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 2');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 3','Marketing','Local development opportunity for students. Connect through real REST API.',130000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 3');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 4','Development','Local development opportunity for students. Connect through real REST API.',140000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 4');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 5','Design','Local development opportunity for students. Connect through real REST API.',150000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 5');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 6','Marketing','Local development opportunity for students. Connect through real REST API.',160000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 6');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 7','Development','Local development opportunity for students. Connect through real REST API.',170000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 7');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 8','Design','Local development opportunity for students. Connect through real REST API.',180000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 8');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 9','Marketing','Local development opportunity for students. Connect through real REST API.',190000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 9');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 10','Development','Local development opportunity for students. Connect through real REST API.',200000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 10');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 11','Design','Local development opportunity for students. Connect through real REST API.',210000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 11');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 12','Marketing','Local development opportunity for students. Connect through real REST API.',220000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 12');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 13','Development','Local development opportunity for students. Connect through real REST API.',230000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 13');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 14','Design','Local development opportunity for students. Connect through real REST API.',240000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 14');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 15','Marketing','Local development opportunity for students. Connect through real REST API.',250000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 15');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 16','Development','Local development opportunity for students. Connect through real REST API.',260000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'OPEN',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 16');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 17','Design','Local development opportunity for students. Connect through real REST API.',270000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'FILLED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 17');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 18','Marketing','Local development opportunity for students. Connect through real REST API.',280000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'FILLED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 18');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 19','Development','Local development opportunity for students. Connect through real REST API.',290000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'FILLED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 19');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 20','Design','Local development opportunity for students. Connect through real REST API.',300000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'FILLED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 20');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 21','Marketing','Local development opportunity for students. Connect through real REST API.',310000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'COMPLETED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 21');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 22','Development','Local development opportunity for students. Connect through real REST API.',320000,'Ho Chi Minh City','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'COMPLETED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 22');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 23','Design','Local development opportunity for students. Connect through real REST API.',330000,'Ha Noi','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'COMPLETED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.a@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 23');
INSERT INTO jobs(employer_id,title,category,description,salary,location,work_schedule,deadline,status,removed_by_admin,created_at,updated_at)
SELECT u.id,'Demo MySQL Opportunity 24','Marketing','Local development opportunity for students. Connect through real REST API.',340000,'Da Nang','Part-time',DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),'COMPLETED',FALSE,NOW(6),NOW(6)
FROM users u WHERE u.email='employer.b@demo.local' AND NOT EXISTS(SELECT 1 FROM jobs j WHERE j.title='Demo MySQL Opportunity 24');
COMMIT;
-- Seed status rules do not imply approved applicants were migrated.
-- Use the real employer workflow to create applications and test transitions.

