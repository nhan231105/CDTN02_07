-- Chế độ mùa thi: sinh viên bật để tạm ẩn/giảm gợi ý việc làm mới trong thời gian ôn, thi
ALTER TABLE student_profiles
  ADD COLUMN exam_mode BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN exam_start_date DATE NULL,
  ADD COLUMN exam_end_date DATE NULL;
