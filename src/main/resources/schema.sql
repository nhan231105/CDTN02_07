CREATE DATABASE IF NOT EXISTS freelance_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE freelance_platform;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_EMPLOYER', 'ROLE_STUDENT'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS profiles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    cv_url VARCHAR(500),
    company_name VARCHAR(255),
    company_description TEXT,
    company_website VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_profiles_user_id (user_id),
    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    employer_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    salary DECIMAL(15, 2),
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_jobs_employer_id (employer_id),
    KEY idx_jobs_status (status),
    CONSTRAINT fk_jobs_employer
        FOREIGN KEY (employer_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_jobs_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'CLOSED'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    job_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_applications_student_job (student_id, job_id),
    KEY idx_applications_job_id (job_id),
    CONSTRAINT fk_applications_student
        FOREIGN KEY (student_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_applications_job
        FOREIGN KEY (job_id) REFERENCES jobs (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_applications_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
) ENGINE = InnoDB;
