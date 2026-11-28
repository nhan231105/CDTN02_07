-- CampusWork seed audit for MySQL 8.x, including schemas before migration 09.
-- Run only after selecting and confirming the intended database.
-- Result sets contain schema metadata and counts, never account data or secrets.
START TRANSACTION READ ONLY, WITH CONSISTENT SNAPSHOT;

SELECT DATABASE() AS selected_schema, VERSION() AS mysql_version;
SHOW SESSION STATUS LIKE 'Ssl_cipher';

SHOW CREATE TABLE users;
SHOW CREATE TABLE employer_profiles;
SHOW CREATE TABLE jobs;
SHOW CREATE TABLE stored_files;

SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('users','employer_profiles','jobs','stored_files','stored_image_bytes')
ORDER BY table_name;

-- Empty result for stored_image_bytes is expected before migration 09.
SELECT table_name, column_name, column_type, is_nullable,
       character_set_name, collation_name, column_key
FROM information_schema.columns
WHERE table_schema = DATABASE() AND (
    (table_name = 'stored_files' AND column_name = 'ref')
    OR table_name = 'stored_image_bytes'
)
ORDER BY table_name, ordinal_position;

SELECT table_name, column_name, referenced_table_name, referenced_column_name
FROM information_schema.key_column_usage
WHERE constraint_schema = DATABASE()
  AND table_name IN ('employer_profiles','jobs','stored_files','stored_image_bytes')
  AND referenced_table_name IS NOT NULL
ORDER BY table_name, column_name;

SELECT event_object_table, trigger_name, event_manipulation, action_timing
FROM information_schema.triggers
WHERE trigger_schema = DATABASE()
  AND event_object_table IN ('users','employer_profiles','jobs','stored_files','stored_image_bytes')
ORDER BY event_object_table, trigger_name;

SELECT
  (SELECT COUNT(*) FROM users) AS users_now,
  (SELECT COUNT(*) FROM users WHERE role = 'EMPLOYER') AS employers_now,
  (SELECT COUNT(*) FROM employer_profiles) AS employer_profiles_now,
  (SELECT COUNT(*) FROM jobs) AS jobs_now,
  (SELECT COUNT(*) FROM stored_files) AS stored_files_now,
  (SELECT COUNT(*) FROM jobs
   WHERE status = 'OPEN' AND removed_by_admin = FALSE
     AND deadline >= DATE(UTC_TIMESTAMP() + INTERVAL 7 HOUR)) AS public_jobs_now;

SELECT
  (SELECT COUNT(*) FROM employer_profiles p LEFT JOIN users u ON u.id = p.user_id
   WHERE u.id IS NULL OR u.role <> 'EMPLOYER') AS profiles_with_invalid_owner,
  (SELECT COUNT(*) FROM jobs j LEFT JOIN users u ON u.id = j.employer_id
   WHERE u.id IS NULL OR u.role <> 'EMPLOYER') AS jobs_with_invalid_owner,
  (SELECT COUNT(*) FROM stored_files f LEFT JOIN users u ON u.id = f.owner_id
   WHERE u.id IS NULL) AS files_without_owner,
  (SELECT COUNT(*) FROM employer_profiles p LEFT JOIN stored_files f ON BINARY f.ref = BINARY p.logo_ref
   WHERE p.logo_ref IS NOT NULL AND p.logo_ref <> ''
     AND (f.ref IS NULL OR f.owner_id <> p.user_id OR f.content_type NOT LIKE 'image/%')) AS profiles_with_invalid_logo;

-- UUID v3 reproduces Java UUID.nameUUIDFromBytes for the seed catalog.
WITH RECURSIVE sequence AS (
  SELECT 1 AS n
  UNION ALL SELECT n + 1 FROM sequence WHERE n < 15
), candidates AS (
  SELECT n, CONCAT('cw', LPAD(n, 2, '0')) AS company_key,
         CONCAT('00000000', LPAD(n, 2, '0')) AS phone,
         MD5(CONCAT('campuswork-demo-v1/logo/cw', LPAD(n, 2, '0'))) AS hash_hex
  FROM sequence
), refs AS (
  SELECT company_key, phone, IF(n <= 10, 3, 2) AS planned_jobs,
         CONCAT(SUBSTRING(hash_hex, 1, 8), '-', SUBSTRING(hash_hex, 9, 4),
                '-3', SUBSTRING(hash_hex, 14, 3), '-',
                SUBSTRING('89ab', 1 + MOD(CONV(SUBSTRING(hash_hex, 17, 1), 16, 10), 4), 1),
                SUBSTRING(hash_hex, 18, 3), '-', SUBSTRING(hash_hex, 21, 12)) AS logo_ref
  FROM candidates
)
SELECT company_key, planned_jobs,
       EXISTS(SELECT 1 FROM users WHERE email = CONCAT(company_key, '@campuswork-seed.example')) AS email_match,
       EXISTS(SELECT 1 FROM users WHERE phone = refs.phone) AS phone_match,
       EXISTS(SELECT 1 FROM stored_files WHERE ref = logo_ref) AS logo_ref_match,
       EXISTS(SELECT 1 FROM stored_files WHERE storage_name = CONCAT(logo_ref, '.png')) AS logo_filename_match
FROM refs
ORDER BY company_key;

ROLLBACK;
