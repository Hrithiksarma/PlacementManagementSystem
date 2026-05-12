-- ============================================================
-- PRMS — File 01: Database Schema
-- Placement Records Management System
-- IIIT Guwahati | CS621 Database Systems
-- 8 Tables, 3NF/4NF normalised, InnoDB engine
-- ============================================================

DROP DATABASE IF EXISTS placement_records_db;
CREATE DATABASE placement_records_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE placement_records_db;

-- ── TABLE 1: Departments ─────────────────────────────────────
-- 8 departments: B.Tech/M.Tech × CSE/ECE × 2 sections each
CREATE TABLE Departments (
    dept_id     INT             PRIMARY KEY AUTO_INCREMENT,
    dept_name   VARCHAR(50)     NOT NULL,
    program     ENUM('B.Tech','M.Tech') NOT NULL,
    branch      ENUM('CSE','ECE')       NOT NULL,
    section     VARCHAR(10)     NOT NULL,           -- e.g. C13, ME14
    UNIQUE KEY uq_dept_section (section)
) ENGINE=InnoDB;

-- ── TABLE 2: Students ────────────────────────────────────────
CREATE TABLE Students (
    student_id      INT             PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    dept_id         INT             NOT NULL,
    batch_year      YEAR            NOT NULL,
    cgpa            DECIMAL(4,2)    NOT NULL,
    active_backlogs INT             NOT NULL DEFAULT 0,
    phone           VARCHAR(15)     UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    placement_tier  ENUM('Unplaced','Normal','Dream','Super Dream')
                                    NOT NULL DEFAULT 'Unplaced',
    CONSTRAINT fk_student_dept
        FOREIGN KEY (dept_id) REFERENCES Departments(dept_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_student_cgpa
        CHECK (cgpa >= 0.00 AND cgpa <= 10.00),
    CONSTRAINT chk_student_backlogs
        CHECK (active_backlogs >= 0),
    CONSTRAINT chk_student_batch_year
        CHECK (batch_year >= 2015 AND batch_year <= 2035),
    CONSTRAINT chk_student_name_nonempty
        CHECK (TRIM(name) <> ''),
    CONSTRAINT chk_student_name_no_numbers
        CHECK (name NOT REGEXP '[0-9]'),
    CONSTRAINT chk_student_phone
        CHECK (phone IS NULL OR phone REGEXP '^[6-9][0-9]{9}$'),
    CONSTRAINT chk_student_email
        CHECK (
            email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+){1,2}$'
            AND email NOT LIKE '%,%'
            AND email NOT LIKE '% %'
        )
) ENGINE=InnoDB;

-- ── TABLE 3: Companies ───────────────────────────────────────
CREATE TABLE Companies (
    company_id  INT             PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    sector      VARCHAR(50)     NOT NULL,                   -- IT, Core, Finance, Consulting
    tier        ENUM('Normal','Dream','Super Dream') NOT NULL,
    website     VARCHAR(200),
    CONSTRAINT chk_company_name_nonempty
        CHECK (TRIM(name) <> ''),
    CONSTRAINT chk_company_website
        CHECK (website IS NULL OR website REGEXP '^https?://.+')
) ENGINE=InnoDB;

-- ── TABLE 4: HR_Contacts ─────────────────────────────────────
-- Separated from Drives (3NF — transitive dependency removed)
CREATE TABLE HR_Contacts (
    hr_id       INT             PRIMARY KEY AUTO_INCREMENT,
    company_id  INT             NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    contact_no  VARCHAR(15),
    email       VARCHAR(100)    UNIQUE,
    CONSTRAINT fk_hr_company
        FOREIGN KEY (company_id) REFERENCES Companies(company_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_hr_phone
        CHECK (contact_no IS NULL OR contact_no REGEXP '^[6-9][0-9]{9}$'),
    CONSTRAINT chk_hr_email
        CHECK (
            email IS NULL
            OR (
                email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+){1,2}$'
                AND email NOT LIKE '%,%'
                AND email NOT LIKE '% %'
            )
        )
) ENGINE=InnoDB;

-- ── TABLE 5: Drives ──────────────────────────────────────────
CREATE TABLE Drives (
    drive_id        INT             PRIMARY KEY AUTO_INCREMENT,
    company_id      INT             NOT NULL,
    hr_id           INT,
    drive_date      DATE            NOT NULL,
    role_offered    VARCHAR(100)    NOT NULL,
    package_lpa     DECIMAL(5,2),
    drive_type      ENUM('Internship','FTE')     NOT NULL,
    status          ENUM('Upcoming','Ongoing','Completed') NOT NULL DEFAULT 'Upcoming',
    CONSTRAINT fk_drive_company
        FOREIGN KEY (company_id) REFERENCES Companies(company_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_drive_hr
        FOREIGN KEY (hr_id) REFERENCES HR_Contacts(hr_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_drive_package_positive
        CHECK (package_lpa IS NULL OR package_lpa > 0),
    CONSTRAINT chk_drive_package_max
        CHECK (package_lpa IS NULL OR package_lpa <= 200.00),
    CONSTRAINT chk_drive_date_min
        CHECK (drive_date >= '2010-01-01')
) ENGINE=InnoDB;

-- ── TABLE 6: DriveEligibility ────────────────────────────────
-- Separated from Drives (4NF — multi-valued dependency removed)
-- Each drive can have different criteria per department
CREATE TABLE DriveEligibility (
    eligibility_id  INT             PRIMARY KEY AUTO_INCREMENT,
    drive_id        INT             NOT NULL,
    dept_id         INT             NOT NULL,
    min_cgpa        DECIMAL(4,2)    NOT NULL DEFAULT 6.00,
    max_backlogs    INT             NOT NULL DEFAULT 0,
    CONSTRAINT fk_elig_drive
        FOREIGN KEY (drive_id) REFERENCES Drives(drive_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_elig_dept
        FOREIGN KEY (dept_id) REFERENCES Departments(dept_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE KEY uq_drive_dept (drive_id, dept_id),
    CONSTRAINT chk_eligibility_cgpa
        CHECK (min_cgpa >= 0.00 AND min_cgpa <= 10.00),
    CONSTRAINT chk_eligibility_backlogs
        CHECK (max_backlogs >= 0)
) ENGINE=InnoDB;

-- ── TABLE 7: Applications ────────────────────────────────────
CREATE TABLE Applications (
    application_id  INT             PRIMARY KEY AUTO_INCREMENT,
    student_id      INT             NOT NULL,
    drive_id        INT             NOT NULL,
    apply_date      DATE            NOT NULL DEFAULT (CURDATE()),
    result          ENUM('Pending','Selected','Rejected') NOT NULL DEFAULT 'Pending',
    offer_letter_date DATE          NULL,
    waitlist_rank   INT             NULL,
    CONSTRAINT fk_app_student
        FOREIGN KEY (student_id) REFERENCES Students(student_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_app_drive
        FOREIGN KEY (drive_id) REFERENCES Drives(drive_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_student_drive (student_id, drive_id),
    CONSTRAINT chk_app_waitlist_positive
        CHECK (waitlist_rank IS NULL OR waitlist_rank > 0),
    CONSTRAINT chk_app_offer_after_apply
        CHECK (offer_letter_date IS NULL OR offer_letter_date >= apply_date)
) ENGINE=InnoDB;

-- ── TABLE 8: Rounds ──────────────────────────────────────────
-- Separated from Applications (1NF — repeating group removed)
CREATE TABLE Rounds (
    round_id        INT             PRIMARY KEY AUTO_INCREMENT,
    application_id  INT             NOT NULL,
    round_number    INT             NOT NULL,
    round_type      ENUM('Aptitude','Coding','Technical','HR','GD') NOT NULL,
    score           DECIMAL(5,2)    NULL,
    status          ENUM('Pass','Fail','Pending')   NOT NULL DEFAULT 'Pending',
    remarks         TEXT            NULL,
    CONSTRAINT fk_round_app
        FOREIGN KEY (application_id) REFERENCES Applications(application_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE KEY uq_app_round_num (application_id, round_number),
    CONSTRAINT chk_round_number_positive
        CHECK (round_number > 0),
    CONSTRAINT chk_round_score_nonneg
        CHECK (score IS NULL OR score >= 0)
) ENGINE=InnoDB;

-- ── Verify ───────────────────────────────────────────────────
SHOW TABLES;
