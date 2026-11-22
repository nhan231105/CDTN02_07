-- Select the target database before running this file.
-- Existing columns and their data are preserved on reruns.

SET @exam_mode_ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'student_profiles' AND column_name = 'exam_mode'),
  'SELECT 1',
  'ALTER TABLE student_profiles ADD COLUMN exam_mode BOOLEAN NOT NULL DEFAULT FALSE'
);
PREPARE exam_mode_stmt FROM @exam_mode_ddl;
EXECUTE exam_mode_stmt;
DEALLOCATE PREPARE exam_mode_stmt;

SET @exam_mode_ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'student_profiles' AND column_name = 'exam_start_date'),
  'SELECT 1',
  'ALTER TABLE student_profiles ADD COLUMN exam_start_date DATE NULL'
);
PREPARE exam_mode_stmt FROM @exam_mode_ddl;
EXECUTE exam_mode_stmt;
DEALLOCATE PREPARE exam_mode_stmt;

SET @exam_mode_ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'student_profiles' AND column_name = 'exam_end_date'),
  'SELECT 1',
  'ALTER TABLE student_profiles ADD COLUMN exam_end_date DATE NULL'
);
PREPARE exam_mode_stmt FROM @exam_mode_ddl;
EXECUTE exam_mode_stmt;
DEALLOCATE PREPARE exam_mode_stmt;

SET @exam_mode_ddl = NULL;
