-- Chạy MỘT LẦN trong MySQL Workbench trên DB freelance_marketplace.
USE freelance_marketplace;

ALTER TABLE student_profiles
  ADD COLUMN open_to_work BOOLEAN NOT NULL DEFAULT FALSE;

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