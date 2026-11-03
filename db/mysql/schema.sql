-- MySQL 8.0.16+; preparation only. NOT executed by the agent.
-- Run against a NEW development schema after backup. No DROP/TRUNCATE statements.
CREATE DATABASE IF NOT EXISTS freelance_marketplace
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE freelance_marketplace;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('STUDENT','EMPLOYER','ADMIN') NOT NULL,
  status ENUM('ACTIVE','LOCKED') NOT NULL,
  lock_reason VARCHAR(500),
  contact_changed_at DATETIME(6),
  token_version INT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6),
  CONSTRAINT uq_user_email UNIQUE (email),
  CONSTRAINT uq_user_phone UNIQUE (phone)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_profiles (
  user_id BIGINT NOT NULL PRIMARY KEY,
  avatar_ref VARCHAR(255),
  bio TEXT,
  cv_file_ref VARCHAR(255),
  cv_original_name VARCHAR(255),
  school VARCHAR(150),
  major VARCHAR(150),
  study_year INT,
  skills VARCHAR(500),
  available_shifts VARCHAR(100),
  preferred_area VARCHAR(100),
  open_to_work BOOLEAN NOT NULL DEFAULT FALSE,
  lat DOUBLE,
  lng DOUBLE,
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS employer_profiles (
  user_id BIGINT NOT NULL PRIMARY KEY,
  org_name VARCHAR(150),
  address VARCHAR(255),
  description TEXT,
  logo_ref VARCHAR(255),
  website VARCHAR(255),
  industry VARCHAR(100),
  company_size VARCHAR(50),
  cover_ref VARCHAR(255),
  tagline VARCHAR(150),
  founded_year INT,
  benefits VARCHAR(600),
  CONSTRAINT fk_employer_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stored_files (
  ref VARCHAR(36) NOT NULL PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  size BIGINT NOT NULL,
  storage_name VARCHAR(100) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uq_file_storage_name UNIQUE (storage_name),
  CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT ck_file_size CHECK (size >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS jobs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  employer_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  category VARCHAR(100),
  description TEXT NOT NULL,
  requirements TEXT,
  benefits TEXT,
  salary DECIMAL(12,0),
  location VARCHAR(100),
  work_schedule VARCHAR(200),
  deadline DATE NOT NULL,
  job_type VARCHAR(20) NOT NULL DEFAULT 'PART_TIME',
  pay_type VARCHAR(20) NOT NULL DEFAULT 'HOURLY',
  shifts VARCHAR(100),
  hours_per_week INT,
  no_experience_required BOOLEAN NOT NULL DEFAULT FALSE,
  student_friendly BOOLEAN NOT NULL DEFAULT TRUE,
  address VARCHAR(255),
  start_date DATE,
  duration_months INT,
  headcount INT NOT NULL DEFAULT 1,
  lat DOUBLE,
  lng DOUBLE,
  status ENUM('OPEN','FILLED','COMPLETED') NOT NULL,
  removed_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
  removal_reason VARCHAR(500),
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  INDEX idx_status (status),
  INDEX idx_location (location),
  INDEX idx_category (category),
  INDEX idx_employer (employer_id),
  CONSTRAINT fk_job_employer FOREIGN KEY (employer_id) REFERENCES users(id),
  CONSTRAINT ck_job_salary CHECK (salary IS NULL OR salary >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS saved_jobs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_saved_job_student UNIQUE (student_id,job_id),
  INDEX idx_saved_student (student_id),
  CONSTRAINT fk_saved_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT fk_saved_job FOREIGN KEY (job_id) REFERENCES jobs(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS applications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  job_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  cv_file_ref VARCHAR(255),
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  cover_letter TEXT,
  consent_data_usage BOOLEAN NOT NULL DEFAULT FALSE,
  status ENUM('PENDING','APPROVED','REJECTED','WITHDRAWN') NOT NULL,
  created_at DATETIME(6) NOT NULL,
  decided_at DATETIME(6),
  viewed_at DATETIME(6),
  withdrawn_at DATETIME(6),
  CONSTRAINT uc_job_student UNIQUE (job_id,student_id),
  INDEX idx_app_job (job_id),
  INDEX idx_app_student (student_id),
  INDEX idx_app_status (status),
  CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT fk_application_student FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  job_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  reviewee_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content TEXT,
  is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uc_review_unique UNIQUE (job_id,reviewer_id,reviewee_id),
  INDEX idx_job_review (job_id),
  INDEX idx_reviewer (reviewer_id),
  INDEX idx_reviewee (reviewee_id),
  CONSTRAINT fk_review_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id),
  CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  message VARCHAR(500) NOT NULL,
  status ENUM('UNREAD','READ') NOT NULL,
  related_type VARCHAR(50),
  related_id BIGINT,
  created_at DATETIME(6) NOT NULL,
  INDEX idx_notification_user (user_id),
  INDEX idx_notification_status (status),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Preserved compatibility entity; the complete reporting UI is outside 7 UC.
CREATE TABLE IF NOT EXISTS violation_reports (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  reporter_id BIGINT NOT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status ENUM('PENDING','RESOLVED','REJECTED') NOT NULL,
  admin_action VARCHAR(100),
  created_at DATETIME(6) NOT NULL,
  INDEX idx_reporter (reporter_id),
  INDEX idx_status_report (status),
  CONSTRAINT fk_reporter_user FOREIGN KEY (reporter_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- File references in profiles/applications are validated by the storage service.
-- Their immutable stored_files metadata and physical files must be backed up together.


SHOW TABLES FROM freelance_marketplace;

CREATE TABLE IF NOT EXISTS invitations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  employer_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  message VARCHAR(500),
  status ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL,
  created_at DATETIME(6) NOT NULL,
  responded_at DATETIME(6),
  CONSTRAINT uc_invite_job_student UNIQUE (job_id,student_id),
  INDEX idx_invite_student (student_id),
  INDEX idx_invite_employer (employer_id),
  CONSTRAINT fk_invite_employer FOREIGN KEY (employer_id) REFERENCES users(id),
  CONSTRAINT fk_invite_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT fk_invite_job FOREIGN KEY (job_id) REFERENCES jobs(id)
) ENGINE=InnoDB;
