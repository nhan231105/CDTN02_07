-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: freelance_marketplace
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `freelance_marketplace`
--

/*!40000 DROP DATABASE IF EXISTS `freelance_marketplace`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `freelance_marketplace` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `freelance_marketplace`;

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `cv_file_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cover_letter` text COLLATE utf8mb4_unicode_ci,
  `consent_data_usage` tinyint(1) NOT NULL DEFAULT '0',
  `status` enum('PENDING','APPROVED','REJECTED','WITHDRAWN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `decided_at` datetime(6) DEFAULT NULL,
  `viewed_at` datetime(6) DEFAULT NULL,
  `withdrawn_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uc_job_student` (`job_id`,`student_id`),
  KEY `idx_app_job` (`job_id`),
  KEY `idx_app_student` (`student_id`),
  KEY `idx_app_status` (`status`),
  CONSTRAINT `fk_application_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
  CONSTRAINT `fk_application_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applications`
--

LOCK TABLES `applications` WRITE;
/*!40000 ALTER TABLE `applications` DISABLE KEYS */;
INSERT INTO `applications` VALUES (1,17,2,NULL,'Minh Anh Nguyễn','student.a@demo.local','0900000001','Em muốn tham gia sự kiện này.',1,'APPROVED','2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',NULL,NULL),(2,18,1,'f8fb43af-74f3-407d-b15f-dc0a0c9d624e','Lễ Văn Trần','le.2374802010270@vanlanguni.vn','0852131745','mình muốn ứng tuyển ',1,'APPROVED','2026-11-03 17:48:26.432778','2026-11-03 17:49:18.153004','2026-11-03 17:48:54.335194',NULL),(3,14,1,'f8fb43af-74f3-407d-b15f-dc0a0c9d624e','Lễ Văn Trần','le.2374802010270@vanlanguni.vn','0852131745','fg',1,'WITHDRAWN','2026-11-03 18:56:49.155736',NULL,NULL,'2026-11-03 19:45:27.844827'),(4,16,1,'f8fb43af-74f3-407d-b15f-dc0a0c9d624e','Lễ Văn Trần','le.2374802010270@vanlanguni.vn','0852131745','haha',1,'WITHDRAWN','2026-11-03 19:00:31.658037',NULL,NULL,'2026-11-03 19:45:29.646973');
/*!40000 ALTER TABLE `applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employer_profiles`
--

DROP TABLE IF EXISTS `employer_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employer_profiles` (
  `user_id` bigint NOT NULL,
  `org_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `logo_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_size` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tagline` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `founded_year` int DEFAULT NULL,
  `benefits` varchar(600) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_employer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employer_profiles`
--

LOCK TABLES `employer_profiles` WRITE;
/*!40000 ALTER TABLE `employer_profiles` DISABLE KEYS */;
INSERT INTO `employer_profiles` VALUES (4,'Highlands Coffee Quận 1','25 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh','Chuỗi cà phê tuyển sinh viên làm ca linh hoạt, được đào tạo từ đầu.','cc114925-e097-4c6d-81a3-4318eb0a681f','https://example.com','Dịch vụ - Bán lẻ','10-50 nhân viên','fcbf8767-f913-407f-adb2-5cd41e474bae','Cùng sinh viên lớn lên mỗi ngày',2016,'Lương trả đúng hạn|Chọn ca linh hoạt|Đào tạo từ đầu'),(5,'Trung tâm Anh ngữ Bright Future','Mỹ Đình, Nam Từ Liêm, Hà Nội','Trung tâm ngoại ngữ tuyển trợ giảng, gia sư là sinh viên.',NULL,'https://example.com','Dịch vụ - Bán lẻ','10-50 nhân viên',NULL,NULL,NULL,NULL),(7,'Văn Lễ','Thành Phố Hồ Chí Minh','ABC','d3bcc15b-18cf-4002-869d-a9175b4fdb51',NULL,NULL,NULL,'45836ab6-6fc4-4549-9111-7f4775fdb7a0','Vui vẽ - Hòa đồng - Lương cao',NULL,'Thưởng theo hiệu suất|Cấp đồng phục');
/*!40000 ALTER TABLE `employer_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invitations`
--

DROP TABLE IF EXISTS `invitations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employer_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','ACCEPTED','DECLINED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uc_invite_job_student` (`job_id`,`student_id`),
  KEY `idx_invite_student` (`student_id`),
  KEY `idx_invite_employer` (`employer_id`),
  CONSTRAINT `fk_invite_employer` FOREIGN KEY (`employer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_invite_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
  CONSTRAINT `fk_invite_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invitations`
--

LOCK TABLES `invitations` WRITE;
/*!40000 ALTER TABLE `invitations` DISABLE KEYS */;
INSERT INTO `invitations` VALUES (1,4,2,10,'Mời bạn qua làm nhé','PENDING','2026-11-03 19:39:46.313176',NULL),(2,7,2,18,NULL,'PENDING','2026-11-03 19:43:07.554985',NULL);
/*!40000 ALTER TABLE `invitations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employer_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `requirements` text COLLATE utf8mb4_unicode_ci,
  `benefits` text COLLATE utf8mb4_unicode_ci,
  `salary` decimal(12,0) DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `work_schedule` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deadline` date NOT NULL,
  `job_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PART_TIME',
  `pay_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'HOURLY',
  `shifts` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hours_per_week` int DEFAULT NULL,
  `no_experience_required` tinyint(1) NOT NULL DEFAULT '0',
  `student_friendly` tinyint(1) NOT NULL DEFAULT '1',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `duration_months` int DEFAULT NULL,
  `headcount` int NOT NULL DEFAULT '1',
  `status` enum('OPEN','FILLED','COMPLETED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `removed_by_admin` tinyint(1) NOT NULL DEFAULT '0',
  `removal_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `lat` double DEFAULT NULL,
  `lng` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`location`),
  KEY `idx_category` (`category`),
  KEY `idx_employer` (`employer_id`),
  CONSTRAINT `fk_job_employer` FOREIGN KEY (`employer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_job_salary` CHECK (((`salary` is null) or (`salary` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jobs`
--

LOCK TABLES `jobs` WRITE;
/*!40000 ALTER TABLE `jobs` DISABLE KEYS */;
INSERT INTO `jobs` VALUES (1,4,'Nhân viên phục vụ quán cà phê','Hospitality','Quán cà phê tại trung tâm Quận 1 cần sinh viên phục vụ làm theo ca, lịch xếp linh hoạt theo thời khóa biểu.\nMô tả công việc:\n- Chào đón, ghi order và phục vụ đồ uống cho khách\n- Pha chế các món cơ bản theo công thức có sẵn\n- Dọn bàn, giữ vệ sinh khu vực làm việc\n- Phối hợp với quản lý ca để kiểm kê cuối ngày\nThời gian: 2-3 ca mỗi tuần, mỗi ca 4-5 giờ (sáng hoặc chiều).','- Sinh viên năm 1-4, đang học tại TP.HCM\n- Nhanh nhẹn, trung thực, thân thiện với khách\n- Đi làm đúng giờ, cam kết tối thiểu 3 tháng\n- Không cần kinh nghiệm, được đào tạo từ đầu','- Lương 28.000đ/giờ, trả vào ngày 5 hàng tháng\n- Ăn ca miễn phí, đồng phục do quán cấp\n- Thưởng lễ tết, thưởng chuyên cần\n- Đổi ca linh hoạt vào mùa thi (báo trước 3 ngày)',28000,'TP. Hồ Chí Minh','20 giờ/tuần','2026-12-03','PART_TIME','HOURLY','MORNING,AFTERNOON',20,1,1,'25 Nguyễn Huệ, Quận 1','2026-11-10',6,3,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.768233334,106.7029),(2,4,'Phục vụ nhà hàng ca tối','Hospitality','Nhà hàng cần nhân viên phục vụ ca tối, phù hợp sinh viên có lịch học buổi sáng và chiều.\nMô tả công việc:\n- Đón khách, hướng dẫn chỗ ngồi và giới thiệu thực đơn\n- Ghi món, bưng bê và theo dõi bàn được phân công\n- Hỗ trợ thanh toán và dọn bàn sau khi khách rời đi\nThời gian: 17h30 - 22h30, 3-4 buổi mỗi tuần.','- Ngoại hình gọn gàng, giao tiếp tốt\n- Có thể làm việc vào buổi tối, kể cả cuối tuần\n- Ưu tiên người từng làm dịch vụ nhưng không bắt buộc','- Lương 30.000đ/giờ cộng tip chia đều\n- Ăn tối miễn phí tại nhà hàng\n- Được đào tạo quy trình phục vụ chuyên nghiệp\n- Cơ hội lên ca trưởng sau 3 tháng',30000,'TP. Hồ Chí Minh','18 giờ/tuần','2026-12-03','PART_TIME','HOURLY','EVENING',18,1,1,'102 Võ Văn Tần, Quận 3','2026-11-08',4,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.7929,106.671566667),(3,4,'Pha chế trà sữa','Hospitality','Cửa hàng trà sữa tại Hải Châu cần bạn pha chế và hỗ trợ bán hàng.\nMô tả công việc:\n- Nhận order và pha chế đồ uống theo công thức chuẩn\n- Chuẩn bị nguyên liệu, topping trước giờ cao điểm\n- Giữ quầy gọn gàng, vệ sinh dụng cụ\nThời gian: ca chiều hoặc tối, khoảng 20 giờ mỗi tuần.','- Không cần kinh nghiệm, được đào tạo 3 ngày\n- Sạch sẽ, cẩn thận, chịu khó học hỏi\n- Sinh viên các trường tại Đà Nẵng','- Lương 27.000đ/giờ\n- Hỗ trợ giữ xe, thưởng chuyên cần\n- Được uống thử sản phẩm mới\n- Lịch làm linh hoạt theo lịch học',27000,'Đà Nẵng','20 giờ/tuần','2026-11-28','PART_TIME','HOURLY','AFTERNOON,EVENING',20,1,1,'Lê Duẩn, Hải Châu','2026-11-06',3,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',16.0284,108.2082),(4,4,'Thu ngân cửa hàng tiện lợi','Sales','Cửa hàng tiện lợi tại Cầu Giấy tuyển thu ngân ca xoay, phù hợp sinh viên học ngày.\nMô tả công việc:\n- Quét mã, thanh toán và xuất hóa đơn cho khách\n- Sắp xếp, kiểm tra hạn sử dụng hàng hóa\n- Hỗ trợ khách tìm sản phẩm, giữ cửa hàng sạch sẽ\n- Kiểm quỹ cuối ca cùng quản lý','- Cẩn thận với tiền, tính nhẩm tốt\n- Làm được ca chiều hoặc tối, tối thiểu 20 giờ/tuần\n- Trung thực, có trách nhiệm','- Lương 27.000đ/giờ\n- Giảm giá nhân viên, thưởng doanh số\n- Đào tạo vận hành máy POS\n- Có thể chuyển sang làm bán thời gian lâu dài',27000,'Hà Nội','24 giờ/tuần','2026-12-01','PART_TIME','HOURLY','AFTERNOON,EVENING',24,1,1,'18 Cầu Giấy','2026-11-09',6,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',21.027166667,105.828866667),(5,4,'Nhân viên bán hàng cuối tuần','Sales','Tư vấn và bán hàng tại trung tâm thương mại vào thứ 7 và chủ nhật.\nMô tả công việc:\n- Chào đón và tư vấn sản phẩm cho khách\n- Giới thiệu chương trình khuyến mãi\n- Hỗ trợ trưng bày và kiểm kê hàng','- Giao tiếp tốt, chịu được áp lực\n- Làm được cả hai ngày cuối tuần\n- Không cần kinh nghiệm','- Lương 180.000đ/ca\n- Hoa hồng theo doanh số\n- Được đào tạo kỹ năng bán hàng',180000,'Đà Nẵng','Cuối tuần','2026-11-23','PART_TIME','SHIFT','WEEKEND',12,1,1,'Vincom Ngô Quyền, Sơn Trà','2026-11-10',3,4,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',16.077733333,108.2122),(6,4,'Phát tờ rơi và PG sự kiện','Sales','Hỗ trợ tổ chức sự kiện tại Công viên APEC.\nMô tả công việc:\n- Phát tờ rơi, giới thiệu sản phẩm cho người tham dự\n- Hướng dẫn khách tham gia trò chơi, check-in\n- Hỗ trợ ban tổ chức dọn dẹp cuối ngày','- Nhiệt tình, ngoại hình ưa nhìn\n- Có thể đứng và đi lại nhiều giờ ngoài trời\n- Đến đúng giờ tập trung','- Lương 300.000đ/ngày, trả cuối ngày\n- Cung cấp áo đồng phục và nước uống\n- Được hỗ trợ ăn trưa',300000,'Đà Nẵng','Cuối tuần','2026-11-17','SEASONAL','DAILY','WEEKEND',8,1,1,'Công viên APEC','2026-11-07',1,10,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',16.035733334,108.180866667),(7,4,'Nhân viên đóng gói đơn hàng online','Sales','Kho hàng thương mại điện tử cần nhân viên đóng gói đơn.\nMô tả công việc:\n- Kiểm tra sản phẩm theo đơn hàng\n- Đóng gói, dán nhãn và chuyển cho đơn vị vận chuyển\n- Sắp xếp kho gọn gàng','- Cẩn thận, làm việc nhóm tốt\n- Có thể làm ca sáng hoặc tối\n- Sức khỏe tốt, có thể bê vác nhẹ','- Lương 26.000đ/giờ\n- Thưởng đơn vượt chỉ tiêu\n- Hỗ trợ cơm trưa',26000,'TP. Hồ Chí Minh','20 giờ/tuần','2026-12-03','PART_TIME','HOURLY','MORNING,EVENING',20,1,1,'Kho Tân Bình','2026-11-05',6,5,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.7829,106.7149),(8,4,'Cộng tác viên Content mạng xã hội','Marketing','Cộng tác viên viết nội dung cho fanpage và TikTok của thương hiệu, làm việc từ xa.\nMô tả công việc:\n- Lên ý tưởng và viết 15-20 bài mỗi tháng\n- Viết caption, kịch bản video ngắn\n- Theo dõi hiệu quả bài viết và đề xuất cải thiện','- Viết tốt, hiểu xu hướng mạng xã hội\n- Có sản phẩm mẫu hoặc portfolio là lợi thế\n- Chủ động, giao bài đúng hạn','- Thu nhập 3.000.000đ/tháng\n- Thưởng bài lan tỏa\n- Làm việc từ xa, tự sắp xếp thời gian\n- Có thêm portfolio thực tế',3000000,'TP. Hồ Chí Minh','Linh hoạt','2026-12-03','FREELANCE','PROJECT','FLEXIBLE',10,0,1,'Làm việc từ xa','2026-11-08',3,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.807566666,106.683566667),(9,4,'Trực page và chốt đơn','Marketing','Trả lời tin nhắn khách và chốt đơn vào buổi tối, làm tại nhà.\nMô tả công việc:\n- Tư vấn sản phẩm qua Messenger, Zalo\n- Chốt đơn và chuyển thông tin cho bộ phận kho\n- Báo cáo số đơn cuối ngày','- Gõ nhanh, kiên nhẫn, lịch sự\n- Có máy tính hoặc điện thoại, mạng ổn định\n- Làm được khung 19h-22h','- Lương 25.000đ/giờ\n- Thưởng theo số đơn chốt\n- Làm việc tại nhà',25000,'Hà Nội','15 giờ/tuần','2026-12-03','PART_TIME','HOURLY','EVENING',15,1,1,'Làm việc từ xa','2026-11-06',6,3,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',21.017166667,105.8722),(10,4,'Quay dựng video TikTok cho quán','Marketing','Quay và dựng video ngắn giới thiệu quán, khoảng 8 video mỗi tháng.\nMô tả công việc:\n- Lên ý tưởng, quay và dựng video 15-60 giây\n- Chỉnh màu, thêm nhạc và phụ đề\n- Bàn giao đúng lịch đăng đã thống nhất','- Biết CapCut hoặc Premiere\n- Có thiết bị quay cơ bản\n- Có sản phẩm video mẫu','- Thu nhập 4.000.000đ/tháng\n- Được dùng sản phẩm của quán\n- Chủ động thời gian làm việc',4000000,'TP. Hồ Chí Minh','Linh hoạt','2026-12-03','FREELANCE','PROJECT','FLEXIBLE',10,0,1,'Quận Phú Nhuận','2026-11-10',3,1,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.790233333,106.687566667),(11,5,'Gia sư Toán lớp 9','Education','Dạy kèm Toán lớp 9 tại nhà học sinh, 2 buổi mỗi tuần.\nMô tả công việc:\n- Ôn tập kiến thức, giải bài tập theo chương trình\n- Luyện đề ôn thi vào lớp 10\n- Trao đổi tiến độ với phụ huynh hàng tháng','- Sinh viên sư phạm hoặc điểm Toán cao\n- Kiên nhẫn, có phương pháp giải thích dễ hiểu\n- Có thể dạy buổi tối hoặc cuối tuần','- Học phí 150.000đ/giờ\n- Thưởng khi học sinh đạt kết quả tốt\n- Được trung tâm cung cấp tài liệu',150000,'Hà Nội','6 giờ/tuần','2026-12-03','PART_TIME','HOURLY','EVENING,WEEKEND',6,0,1,'Khu đô thị Mỹ Đình','2026-11-10',6,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',20.999833334,105.8762),(12,5,'Gia sư Tiếng Anh giao tiếp','Education','Luyện giao tiếp tiếng Anh cho học sinh cấp 2, giáo trình có sẵn.\nMô tả công việc:\n- Thực hành hội thoại theo chủ đề mỗi buổi\n- Sửa phát âm và bổ sung từ vựng\n- Nhận xét tiến bộ gửi phụ huynh','- IELTS 6.5 hoặc tương đương\n- Phát âm rõ ràng, vui vẻ với trẻ\n- Dạy được 2-3 buổi tối mỗi tuần','- Học phí 180.000đ/giờ\n- Thưởng khi học sinh tiến bộ\n- Được tập huấn phương pháp giảng dạy',180000,'TP. Hồ Chí Minh','8 giờ/tuần','2026-12-03','PART_TIME','HOURLY','EVENING',8,0,1,'Quận Bình Thạnh','2026-11-10',6,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.7729,106.691566667),(13,5,'Trợ giảng lớp lập trình thiếu nhi','Education','Hỗ trợ giảng viên trong lớp Scratch và Python cho trẻ em vào cuối tuần.\nMô tả công việc:\n- Hướng dẫn học viên thực hành trên máy\n- Hỗ trợ giải đáp lỗi cơ bản\n- Điểm danh và giữ trật tự lớp','- Biết lập trình cơ bản\n- Kiên nhẫn, yêu trẻ\n- Làm được sáng thứ 7 và chủ nhật','- Lương 200.000đ/buổi\n- Được đào tạo kỹ năng sư phạm\n- Cấp giấy xác nhận hoạt động',200000,'Đà Nẵng','Cuối tuần','2026-12-03','PART_TIME','SHIFT','WEEKEND',8,0,1,'Hải Châu','2026-11-13',4,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',16.075066666,108.2282),(14,5,'Thiết kế poster, banner part-time','Design','Thiết kế ấn phẩm truyền thông cho trung tâm, làm việc từ xa.\nMô tả công việc:\n- Thiết kế poster, banner, bài đăng mạng xã hội\n- Chỉnh sửa theo góp ý của bộ phận marketing\n- Bàn giao file nguồn và file xuất','- Thành thạo Canva hoặc Photoshop\n- Có gu thẩm mỹ, đúng deadline\n- Có portfolio là lợi thế','- Thu nhập 2.500.000đ/tháng\n- Có thêm portfolio thật\n- Làm việc từ xa, giờ giấc linh hoạt',2500000,'Hà Nội','12 giờ/tuần','2026-12-03','FREELANCE','PROJECT','FLEXIBLE',12,0,1,'Làm việc từ xa','2026-11-08',3,1,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',21.007166667,105.848866667),(15,5,'Lập trình web React bán thời gian','Development','Phát triển giao diện web cho hệ thống quản lý học viên, làm từ xa.\nMô tả công việc:\n- Xây dựng các trang giao diện bằng React\n- Kết nối API do đội backend cung cấp\n- Viết test và sửa lỗi theo review code','- Biết React, JavaScript, Git cơ bản\n- Đọc hiểu tài liệu kỹ thuật tiếng Anh\n- Dành tối thiểu 20 giờ/tuần','- Lương 6.000.000đ/tháng\n- Được mentor kèm cặp\n- Làm việc từ xa\n- Có thể ký tiếp hợp đồng sau 6 tháng',6000000,'TP. Hồ Chí Minh','20 giờ/tuần','2026-12-03','PART_TIME','MONTHLY','FLEXIBLE',20,0,1,'Làm việc từ xa','2026-11-10',6,1,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.780233333,106.7309),(16,5,'Hỗ trợ kiểm thử phần mềm (QA)','Development','Viết test case và kiểm thử ứng dụng web của trung tâm.\nMô tả công việc:\n- Viết kịch bản kiểm thử thủ công\n- Ghi nhận và theo dõi lỗi\n- Phối hợp với lập trình viên để xác nhận sửa lỗi','- Cẩn thận, tư duy logic\n- Biết SQL là lợi thế\n- Có thể làm việc tại văn phòng buổi chiều','- Hỗ trợ 4.500.000đ/tháng\n- Chứng nhận thực tập\n- Được hướng dẫn quy trình QA thực tế',4500000,'Hà Nội','16 giờ/tuần','2026-12-03','INTERNSHIP','MONTHLY','AFTERNOON',16,0,1,'Tòa nhà FPT, Cầu Giấy','2026-11-13',3,2,'OPEN',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',21.0565,105.852866667),(17,4,'Nhân viên phục vụ sự kiện khai trương','Hospitality','Phục vụ trong sự kiện khai trương cửa hàng mới.','Nhanh nhẹn, thân thiện.','Ăn trưa miễn phí.',250000,'TP. Hồ Chí Minh','Cuối tuần','2026-11-13','SEASONAL','DAILY','WEEKEND',8,1,1,'Quận 1','2026-11-03',1,3,'COMPLETED',0,NULL,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946',10.7629,106.668233334),(18,7,'Nhân viên pha chế part-time v2','Sales','Co bao hiem va moi truong tot','co trinhd o tieng anh','moi truong lam viec thoai mai',10000,'TPHCM','Part-time','2026-11-20','PART_TIME','HOURLY','MORNING,EVENING',10,1,0,'325 Hai Bà Trưng TPHCM','2026-11-04',20,1,'COMPLETED',0,NULL,'2026-11-03 17:47:14.118605','2026-11-03 19:45:03.622176',10.787566666,106.703566666),(19,4,'Việc nhẹ lương cao - thu ngân part time','Sales','Việc nhẹ lương cao, chỉ cần đóng cọc 500k giữ chỗ, không cần kinh nghiệm','Nộp phí hồ sơ 200k trước khi nhận việc','Lương 50.000.000đ/tháng',50000000,'TP. Hồ Chí Minh','Linh hoạt','2026-12-31','PART_TIME','MONTHLY',NULL,NULL,0,1,NULL,NULL,NULL,1,'OPEN',0,NULL,'2026-11-03 20:34:35.578130','2026-11-03 20:34:35.578130',NULL,NULL);
/*!40000 ALTER TABLE `jobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('UNREAD','READ') COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`user_id`),
  KEY `idx_notification_status` (`status`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'Chào mừng bạn đến với CampusWork! Hãy cập nhật hồ sơ để nhà tuyển dụng dễ tìm thấy bạn.','UNREAD','JOB',NULL,'2026-11-03 17:23:22.420946'),(2,7,'Có ứng tuyển mới cho công việc: Nhân viên pha chế part-time v2','UNREAD','APPLICATION',2,'2026-11-03 17:48:26.434706'),(3,1,'Nhà tuyển dụng đã xem hồ sơ ứng tuyển: Nhân viên pha chế part-time v2','READ','APPLICATION',2,'2026-11-03 17:48:54.336243'),(4,1,'Đơn ứng tuyển Nhân viên pha chế part-time v2 đã được duyệt','READ','APPLICATION',2,'2026-11-03 17:49:18.156189'),(5,5,'Có ứng tuyển mới cho công việc: Thiết kế poster, banner part-time','UNREAD','APPLICATION',3,'2026-11-03 18:56:49.162850'),(6,5,'Có ứng tuyển mới cho công việc: Hỗ trợ kiểm thử phần mềm (QA)','UNREAD','APPLICATION',4,'2026-11-03 19:00:31.659479'),(7,2,'Highlands Coffee Quận 1 mời bạn ứng tuyển: Quay dựng video TikTok cho quán','UNREAD','INVITATION',1,'2026-11-03 19:39:46.325037'),(8,2,'Văn Lễ mời bạn ứng tuyển: Nhân viên pha chế part-time v2','UNREAD','INVITATION',2,'2026-11-03 19:43:07.558292'),(9,5,'Lễ Văn Trần đã rút đơn ứng tuyển: Thiết kế poster, banner part-time','UNREAD','APPLICATION',3,'2026-11-03 19:45:27.850456'),(10,5,'Lễ Văn Trần đã rút đơn ứng tuyển: Hỗ trợ kiểm thử phần mềm (QA)','UNREAD','APPLICATION',4,'2026-11-03 19:45:29.648073');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `reviewer_id` bigint NOT NULL,
  `reviewee_id` bigint NOT NULL,
  `rating` int NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `is_hidden` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uc_review_unique` (`job_id`,`reviewer_id`,`reviewee_id`),
  KEY `idx_job_review` (`job_id`),
  KEY `idx_reviewer` (`reviewer_id`),
  KEY `idx_reviewee` (`reviewee_id`),
  CONSTRAINT `fk_review_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
  CONSTRAINT `fk_review_reviewee` FOREIGN KEY (`reviewee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_review_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,17,2,4,5,'Quản lý thân thiện, trả lương đúng hẹn và lịch phù hợp sinh viên.',0,'2026-11-03 17:23:22.420946'),(2,18,1,7,4,'tạm ổn',0,'2026-11-03 19:45:40.530293');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saved_jobs`
--

DROP TABLE IF EXISTS `saved_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saved_jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_saved_job_student` (`student_id`,`job_id`),
  KEY `idx_saved_student` (`student_id`),
  KEY `fk_saved_job` (`job_id`),
  CONSTRAINT `fk_saved_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
  CONSTRAINT `fk_saved_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saved_jobs`
--

LOCK TABLES `saved_jobs` WRITE;
/*!40000 ALTER TABLE `saved_jobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `saved_jobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stored_files`
--

DROP TABLE IF EXISTS `stored_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stored_files` (
  `ref` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner_id` bigint NOT NULL,
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `size` bigint NOT NULL,
  `storage_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`ref`),
  UNIQUE KEY `uq_file_storage_name` (`storage_name`),
  KEY `fk_file_owner` (`owner_id`),
  CONSTRAINT `fk_file_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_file_size` CHECK ((`size` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stored_files`
--

LOCK TABLES `stored_files` WRITE;
/*!40000 ALTER TABLE `stored_files` DISABLE KEYS */;
INSERT INTO `stored_files` VALUES ('383ab350-9c99-4e9a-b788-8499fa50c502',2,'tmp-avatar.png','image/png',68,'383ab350-9c99-4e9a-b788-8499fa50c502.png','2026-11-03 20:09:17.018653'),('4180ad59-bdf9-485b-af5d-c3393f797866',1,'drax.jpg','image/jpeg',49586,'4180ad59-bdf9-485b-af5d-c3393f797866.jpg','2026-11-03 20:01:18.641813'),('45836ab6-6fc4-4549-9111-7f4775fdb7a0',7,'tải xuống.jpg','image/jpeg',82099,'45836ab6-6fc4-4549-9111-7f4775fdb7a0.jpg','2026-11-03 20:17:20.266538'),('cc114925-e097-4c6d-81a3-4318eb0a681f',4,'tmp-logo.png','image/png',2703,'cc114925-e097-4c6d-81a3-4318eb0a681f.png','2026-11-03 19:24:15.079347'),('d3bcc15b-18cf-4002-869d-a9175b4fdb51',7,'ChatGPT Image Sep 1, 2026, 12_26_13 PM.png','image/png',1323061,'d3bcc15b-18cf-4002-869d-a9175b4fdb51.png','2026-11-03 20:17:24.721627'),('f8fb43af-74f3-407d-b15f-dc0a0c9d624e',1,'Buoi03_ChuyenDeTotNghiep2.pdf','application/pdf',470360,'f8fb43af-74f3-407d-b15f-dc0a0c9d624e.pdf','2026-11-03 17:48:05.590775'),('fcbf8767-f913-407f-adb2-5cd41e474bae',4,'tmp-cover.jpg','image/jpeg',40488,'fcbf8767-f913-407f-adb2-5cd41e474bae.jpg','2026-11-03 19:24:13.915611');
/*!40000 ALTER TABLE `stored_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_profiles`
--

DROP TABLE IF EXISTS `student_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_profiles` (
  `user_id` bigint NOT NULL,
  `avatar_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `cv_file_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cv_original_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `school` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `study_year` int DEFAULT NULL,
  `skills` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `available_shifts` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `preferred_area` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `open_to_work` tinyint(1) NOT NULL DEFAULT '0',
  `lat` double DEFAULT NULL,
  `lng` double DEFAULT NULL,
  `exam_mode` tinyint(1) NOT NULL DEFAULT '0',
  `exam_start_date` date DEFAULT NULL,
  `exam_end_date` date DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_profiles`
--

LOCK TABLES `student_profiles` WRITE;
/*!40000 ALTER TABLE `student_profiles` DISABLE KEYS */;
INSERT INTO `student_profiles` VALUES (1,'4180ad59-bdf9-485b-af5d-c3393f797866','Tôi muốn làm việc','f8fb43af-74f3-407d-b15f-dc0a0c9d624e','Buoi03_ChuyenDeTotNghiep2.pdf','Trường đại học : Văn Lang','CNTT',4,NULL,'MORNING','Hà Nội',1,21.185570198572137,105.68602098946982,0,NULL,NULL),(2,'383ab350-9c99-4e9a-b788-8499fa50c502','Sinh viên năm 2 ngành Quản trị kinh doanh, rảnh các buổi tối và cuối tuần.',NULL,NULL,'Đại học Kinh tế TP.HCM','Quản trị kinh doanh',3,'Giao tiếp, Excel, Tiếng Anh cơ bản','EVENING,WEEKEND','Phường Xuân Hòa, Thành phố Hồ Chí Minh',1,10.78026145732541,106.69256687164308,0,NULL,NULL),(3,NULL,'Sinh viên năm 3 ngành Công nghệ thông tin, thích làm việc linh hoạt.',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,NULL);
/*!40000 ALTER TABLE `student_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('STUDENT','EMPLOYER','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','LOCKED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `lock_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_changed_at` datetime(6) DEFAULT NULL,
  `token_version` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_email` (`email`),
  UNIQUE KEY `uq_user_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Lễ Văn Trần','le.2374802010270@vanlanguni.vn','0852131745','$2a$10$bfOfTqOpt8nOs9Oy8RaOK.leb1TAPGSB/XR55.nmVo3SEekpzZLqW','STUDENT','ACTIVE',NULL,'2026-11-03 18:20:53.506001',0,'2026-11-03 17:18:39.561848','2026-11-03 18:20:53.507115'),(2,'Minh Anh Nguyễn','student.a@demo.local','0900000001','$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG','STUDENT','ACTIVE',NULL,NULL,0,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946'),(3,'Hoàng Bảo Trần','student.b@demo.local','0900000002','$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG','STUDENT','ACTIVE',NULL,NULL,0,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946'),(4,'Highlands Coffee Quận 1','employer.a@demo.local','0900000003','$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG','EMPLOYER','ACTIVE',NULL,NULL,0,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946'),(5,'Trung tâm Anh ngữ Bright Future','employer.b@demo.local','0900000004','$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG','EMPLOYER','ACTIVE',NULL,NULL,0,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946'),(6,'Quản trị viên','admin@demo.local','0900000005','$2a$10$6kzedD8114slbL9yb0coRuaSoaRsKAH8Kp8IqQ9.ZOytMBFXPmrRG','ADMIN','ACTIVE',NULL,NULL,0,'2026-11-03 17:23:22.420946','2026-11-03 17:23:22.420946'),(7,'Văn Lễ','tranvanletk@gmail.com','0833325172','$2a$10$JY2YrbWKcLK4BjF18lZnHu4FOckE3FyU1UqBEd4zW0E1Wx7VuX/XS','EMPLOYER','ACTIVE',NULL,NULL,0,'2026-11-03 17:44:46.594258','2026-11-03 17:44:46.594258');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `violation_reports`
--

DROP TABLE IF EXISTS `violation_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `violation_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','RESOLVED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `admin_action` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reporter` (`reporter_id`),
  KEY `idx_status_report` (`status`),
  CONSTRAINT `fk_reporter_user` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `violation_reports`
--

LOCK TABLES `violation_reports` WRITE;
/*!40000 ALTER TABLE `violation_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `violation_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'freelance_marketplace'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-03 21:04:23
