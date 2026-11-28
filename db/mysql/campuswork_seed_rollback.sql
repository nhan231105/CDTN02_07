-- Logical rollback of one seed company. Keep ROLLBACK for the first rehearsal.
-- Fill all four values from one row in the receipt produced by that apply.
-- Copy the job_ids field with semicolons. Use a single MySQL session.
SET @seed_user = NULL;
SET @seed_logo = NULL;
SET @seed_job_ids = NULL;
SET @seed_expected_jobs = NULL;

START TRANSACTION;

SELECT u.id, u.status, ep.org_name, f.ref
FROM users u
JOIN employer_profiles ep ON ep.user_id = u.id
JOIN stored_files f ON f.owner_id = u.id AND CAST(f.ref AS BINARY) = CAST(@seed_logo AS BINARY)
WHERE u.id = @seed_user AND u.role = 'EMPLOYER'
  AND LEFT(ep.org_name, 7) = '[DEMO] '
  AND CAST(f.storage_name AS BINARY) = CAST(CONCAT(@seed_logo, '.png') AS BINARY)
FOR UPDATE;

SELECT id, title, status, removed_by_admin
FROM jobs
WHERE employer_id = @seed_user
FOR UPDATE;

SELECT COUNT(*) INTO @seed_owner_matches
FROM users u
JOIN employer_profiles ep ON ep.user_id = u.id
JOIN stored_files f ON f.owner_id = u.id AND CAST(f.ref AS BINARY) = CAST(@seed_logo AS BINARY)
WHERE u.id = @seed_user AND u.role = 'EMPLOYER'
  AND LEFT(ep.org_name, 7) = '[DEMO] '
  AND CAST(f.storage_name AS BINARY) = CAST(CONCAT(@seed_logo, '.png') AS BINARY);

SELECT COUNT(*), COALESCE(SUM(
    FIND_IN_SET(CAST(id AS BINARY), CAST(REPLACE(@seed_job_ids, ';', ',') AS BINARY)) > 0
    AND LEFT(title, 7) = '[DEMO] '
), 0)
INTO @seed_total_jobs, @seed_matching_jobs
FROM jobs
WHERE employer_id = @seed_user;

SET @seed_eligible = COALESCE(
    @seed_expected_jobs IN (2, 3)
    AND @seed_owner_matches = 1
    AND @seed_total_jobs = @seed_expected_jobs
    AND @seed_matching_jobs = @seed_expected_jobs, 0);

SELECT @seed_eligible AS eligible, @seed_matching_jobs AS matching_jobs;

UPDATE jobs
SET removed_by_admin = TRUE, removal_reason = 'Ẩn dữ liệu demo CampusWork'
WHERE @seed_eligible = 1 AND employer_id = @seed_user
  AND FIND_IN_SET(CAST(id AS BINARY), CAST(REPLACE(@seed_job_ids, ';', ',') AS BINARY)) > 0
  AND removed_by_admin = FALSE;
SELECT ROW_COUNT() AS modified_jobs;

UPDATE users
SET status = 'LOCKED', lock_reason = 'Khóa tài khoản demo CampusWork',
    token_version = token_version + 1
WHERE @seed_eligible = 1 AND id = @seed_user AND status = 'ACTIVE';
SELECT ROW_COUNT() AS locked_accounts;

ROLLBACK;
-- After confirmed review and explicit authorization, use COMMIT at the final
-- line in a private copy. Leave the tracked script defaulting to ROLLBACK.
