-- Dữ liệu mẫu cho CampusWork (việc làm part-time sinh viên).
-- Chạy MỘT LẦN sau schema.sql trên database còn trống. Mật khẩu mọi tài khoản mẫu: DemoPass123!
USE freelance_marketplace;

SET @pw = '$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG';
SET @now = CURRENT_TIMESTAMP(6);

INSERT INTO users (full_name,email,phone,password_hash,role,status,token_version,created_at,updated_at) VALUES
 ('Minh Anh Nguyễn','student.a@demo.local','0900000001',@pw,'STUDENT','ACTIVE',0,@now,@now),
 ('Hoàng Bảo Trần','student.b@demo.local','0900000002',@pw,'STUDENT','ACTIVE',0,@now,@now),
 ('Highlands Coffee Quận 1','employer.a@demo.local','0900000003',@pw,'EMPLOYER','ACTIVE',0,@now,@now),
 ('Trung tâm Anh ngữ Bright Future','employer.b@demo.local','0900000004',@pw,'EMPLOYER','ACTIVE',0,@now,@now),
 ('Quản trị viên','admin@demo.local','0900000005',@pw,'ADMIN','ACTIVE',0,@now,@now);

SET @sa = (SELECT id FROM users WHERE email='student.a@demo.local');
SET @sb = (SELECT id FROM users WHERE email='student.b@demo.local');
SET @ea = (SELECT id FROM users WHERE email='employer.a@demo.local');
SET @eb = (SELECT id FROM users WHERE email='employer.b@demo.local');

INSERT INTO student_profiles (user_id,bio) VALUES
 (@sa,'Sinh viên năm 2 ngành Quản trị kinh doanh, rảnh các buổi tối và cuối tuần.'),
 (@sb,'Sinh viên năm 3 ngành Công nghệ thông tin, thích làm việc linh hoạt.');

INSERT INTO employer_profiles (user_id,org_name,address,description) VALUES
 (@ea,'Highlands Coffee Quận 1','25 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh','Chuỗi cà phê tuyển sinh viên làm ca linh hoạt, được đào tạo từ đầu.'),
 (@eb,'Trung tâm Anh ngữ Bright Future','Mỹ Đình, Nam Từ Liêm, Hà Nội','Trung tâm ngoại ngữ tuyển trợ giảng, gia sư là sinh viên.');

-- Tin đang tuyển (hạn nộp 30 ngày, bắt đầu sau 7 ngày)
INSERT INTO jobs (employer_id,title,category,description,requirements,benefits,salary,location,work_schedule,deadline,
                  job_type,pay_type,shifts,hours_per_week,no_experience_required,student_friendly,address,start_date,duration_months,headcount,
                  status,removed_by_admin,created_at,updated_at) VALUES
 (@ea,'Nhân viên phục vụ quán cà phê','Hospitality','Phục vụ khách, pha chế đơn giản và giữ vệ sinh khu vực. Được đào tạo từ đầu, lịch làm theo thời khóa biểu của bạn.','Sinh viên nhanh nhẹn, đúng giờ, thân thiện.','Ăn ca, thưởng lễ tết, đồng phục.',28000,'TP. Hồ Chí Minh','20 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','MORNING,AFTERNOON',20,TRUE,TRUE,'25 Nguyễn Huệ, Quận 1',DATE_ADD(CURDATE(),INTERVAL 7 DAY),6,3,'OPEN',FALSE,@now,@now),
 (@ea,'Phục vụ nhà hàng ca tối','Hospitality','Phục vụ bàn trong ca tối, phù hợp sinh viên học buổi sáng và chiều.','Ngoại hình gọn gàng, giao tiếp tốt.','Ăn tối miễn phí, tip chia đều.',30000,'TP. Hồ Chí Minh','18 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','EVENING',18,TRUE,TRUE,'102 Võ Văn Tần, Quận 3',DATE_ADD(CURDATE(),INTERVAL 5 DAY),4,2,'OPEN',FALSE,@now,@now),
 (@ea,'Pha chế trà sữa','Hospitality','Pha chế đồ uống theo công thức, nhận order và giữ quầy gọn gàng.','Không cần kinh nghiệm, được đào tạo 3 ngày.','Hỗ trợ giữ xe, thưởng chuyên cần.',27000,'Đà Nẵng','20 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 25 DAY),'PART_TIME','HOURLY','AFTERNOON,EVENING',20,TRUE,TRUE,'Lê Duẩn, Hải Châu',DATE_ADD(CURDATE(),INTERVAL 3 DAY),3,2,'OPEN',FALSE,@now,@now),
 (@ea,'Thu ngân cửa hàng tiện lợi','Sales','Thanh toán, sắp xếp hàng hóa và hỗ trợ khách. Ca xoay linh hoạt.','Cẩn thận với tiền, tính nhẩm tốt.','Giảm giá nhân viên, thưởng doanh số.',27000,'Hà Nội','24 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 28 DAY),'PART_TIME','HOURLY','AFTERNOON,EVENING',24,TRUE,TRUE,'18 Cầu Giấy',DATE_ADD(CURDATE(),INTERVAL 6 DAY),6,2,'OPEN',FALSE,@now,@now),
 (@ea,'Nhân viên bán hàng cuối tuần','Sales','Tư vấn và bán hàng tại trung tâm thương mại vào thứ 7, chủ nhật.','Giao tiếp tốt, chịu được áp lực.','Hoa hồng theo doanh số.',180000,'Đà Nẵng','Cuối tuần',DATE_ADD(CURDATE(),INTERVAL 20 DAY),'PART_TIME','SHIFT','WEEKEND',12,TRUE,TRUE,'Vincom Ngô Quyền, Sơn Trà',DATE_ADD(CURDATE(),INTERVAL 7 DAY),3,4,'OPEN',FALSE,@now,@now),
 (@ea,'Phát tờ rơi và PG sự kiện','Sales','Hỗ trợ sự kiện, phát tờ rơi và giới thiệu sản phẩm theo ngày.','Nhiệt tình, ngoại hình ưa nhìn.','Trả lương cuối ngày.',300000,'Đà Nẵng','Cuối tuần',DATE_ADD(CURDATE(),INTERVAL 14 DAY),'SEASONAL','DAILY','WEEKEND',8,TRUE,TRUE,'Công viên APEC',DATE_ADD(CURDATE(),INTERVAL 4 DAY),1,10,'OPEN',FALSE,@now,@now),
 (@ea,'Nhân viên đóng gói đơn hàng online','Sales','Đóng gói, dán nhãn và kiểm tra đơn hàng thương mại điện tử.','Cẩn thận, làm việc nhóm.','Thưởng đơn vượt chỉ tiêu.',26000,'TP. Hồ Chí Minh','20 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','MORNING,EVENING',20,TRUE,TRUE,'Kho Tân Bình',DATE_ADD(CURDATE(),INTERVAL 2 DAY),6,5,'OPEN',FALSE,@now,@now),
 (@ea,'Cộng tác viên Content mạng xã hội','Marketing','Viết bài, lên ý tưởng nội dung cho fanpage và TikTok, làm việc từ xa.','Viết tốt, hiểu xu hướng mạng xã hội.','Có portfolio, thưởng bài lan tỏa.',3000000,'TP. Hồ Chí Minh','Linh hoạt',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'FREELANCE','PROJECT','FLEXIBLE',10,FALSE,TRUE,'Làm việc từ xa',DATE_ADD(CURDATE(),INTERVAL 5 DAY),3,2,'OPEN',FALSE,@now,@now),
 (@ea,'Trực page và chốt đơn','Marketing','Trả lời tin nhắn khách và chốt đơn buổi tối, làm tại nhà.','Gõ nhanh, kiên nhẫn.','Thưởng theo đơn chốt.',25000,'Hà Nội','15 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','EVENING',15,TRUE,TRUE,'Làm việc từ xa',DATE_ADD(CURDATE(),INTERVAL 3 DAY),6,3,'OPEN',FALSE,@now,@now),
 (@ea,'Quay dựng video TikTok cho quán','Marketing','Quay và dựng video ngắn giới thiệu quán, 8 video mỗi tháng.','Biết CapCut hoặc Premiere.','Được dùng sản phẩm quán.',4000000,'TP. Hồ Chí Minh','Linh hoạt',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'FREELANCE','PROJECT','FLEXIBLE',10,FALSE,TRUE,'Quận Phú Nhuận',DATE_ADD(CURDATE(),INTERVAL 7 DAY),3,1,'OPEN',FALSE,@now,@now),
 (@eb,'Gia sư Toán lớp 9','Education','Dạy kèm Toán 9 tại nhà học sinh, 2 buổi mỗi tuần.','Sinh viên sư phạm hoặc điểm Toán cao.','Học phí thỏa thuận theo giờ.',150000,'Hà Nội','6 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','EVENING,WEEKEND',6,FALSE,TRUE,'Khu đô thị Mỹ Đình',DATE_ADD(CURDATE(),INTERVAL 7 DAY),6,2,'OPEN',FALSE,@now,@now),
 (@eb,'Gia sư Tiếng Anh giao tiếp','Education','Luyện giao tiếp cho học sinh cấp 2, giáo trình có sẵn.','IELTS 6.5 hoặc tương đương.','Thưởng khi học sinh tiến bộ.',180000,'TP. Hồ Chí Minh','8 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','HOURLY','EVENING',8,FALSE,TRUE,'Quận Bình Thạnh',DATE_ADD(CURDATE(),INTERVAL 7 DAY),6,2,'OPEN',FALSE,@now,@now),
 (@eb,'Trợ giảng lớp lập trình thiếu nhi','Education','Hỗ trợ giảng viên trong lớp Scratch và Python cho trẻ em vào cuối tuần.','Biết lập trình cơ bản, kiên nhẫn với trẻ.','Được đào tạo sư phạm.',200000,'Đà Nẵng','Cuối tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','SHIFT','WEEKEND',8,FALSE,TRUE,'Hải Châu',DATE_ADD(CURDATE(),INTERVAL 10 DAY),4,2,'OPEN',FALSE,@now,@now),
 (@eb,'Thiết kế poster, banner part-time','Design','Thiết kế ấn phẩm truyền thông cho trung tâm, làm từ xa.','Thành thạo Canva hoặc Photoshop.','Có thêm portfolio thật.',2500000,'Hà Nội','12 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'FREELANCE','PROJECT','FLEXIBLE',12,FALSE,TRUE,'Làm việc từ xa',DATE_ADD(CURDATE(),INTERVAL 5 DAY),3,1,'OPEN',FALSE,@now,@now),
 (@eb,'Lập trình web React bán thời gian','Development','Phát triển giao diện web cho hệ thống quản lý học viên, làm từ xa.','Biết React, Git cơ bản.','Mentor kèm cặp, làm việc từ xa.',6000000,'TP. Hồ Chí Minh','20 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'PART_TIME','MONTHLY','FLEXIBLE',20,FALSE,TRUE,'Làm việc từ xa',DATE_ADD(CURDATE(),INTERVAL 7 DAY),6,1,'OPEN',FALSE,@now,@now),
 (@eb,'Hỗ trợ kiểm thử phần mềm (QA)','Development','Viết test case và kiểm thử ứng dụng web của trung tâm.','Cẩn thận, biết SQL là lợi thế.','Chứng nhận thực tập.',4500000,'Hà Nội','16 giờ/tuần',DATE_ADD(CURDATE(),INTERVAL 30 DAY),'INTERNSHIP','MONTHLY','AFTERNOON',16,FALSE,TRUE,'Tòa nhà FPT, Cầu Giấy',DATE_ADD(CURDATE(),INTERVAL 10 DAY),3,2,'OPEN',FALSE,@now,@now);

-- Một việc đã hoàn thành kèm đánh giá để thử trang nhà tuyển dụng
INSERT INTO jobs (employer_id,title,category,description,requirements,benefits,salary,location,work_schedule,deadline,
                  job_type,pay_type,shifts,hours_per_week,no_experience_required,student_friendly,address,start_date,duration_months,headcount,
                  status,removed_by_admin,created_at,updated_at) VALUES
 (@ea,'Nhân viên phục vụ sự kiện khai trương','Hospitality','Phục vụ trong sự kiện khai trương cửa hàng mới.','Nhanh nhẹn, thân thiện.','Ăn trưa miễn phí.',250000,'TP. Hồ Chí Minh','Cuối tuần',DATE_ADD(CURDATE(),INTERVAL 10 DAY),'SEASONAL','DAILY','WEEKEND',8,TRUE,TRUE,'Quận 1',CURDATE(),1,3,'COMPLETED',FALSE,@now,@now);
SET @done = LAST_INSERT_ID();

INSERT INTO applications (job_id,student_id,full_name,email,phone,cover_letter,consent_data_usage,status,created_at,decided_at)
 VALUES (@done,@sa,'Minh Anh Nguyễn','student.a@demo.local','0900000001','Em muốn tham gia sự kiện này.',TRUE,'APPROVED',@now,@now);

INSERT INTO reviews (job_id,reviewer_id,reviewee_id,rating,content,is_hidden,created_at)
 VALUES (@done,@sa,@ea,5,'Quản lý thân thiện, trả lương đúng hẹn và lịch phù hợp sinh viên.',FALSE,@now);

INSERT INTO notifications (user_id,message,status,related_type,related_id,created_at)
 VALUES (@sa,'Chào mừng bạn đến với CampusWork! Hãy cập nhật hồ sơ để nhà tuyển dụng dễ tìm thấy bạn.','UNREAD','JOB',NULL,@now);

SELECT COUNT(*) AS so_tin_viec FROM jobs;
