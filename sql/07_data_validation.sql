-- ============================================================
-- PRMS — File 07: Data Validation (CHECK Constraints)
-- Fixes professor-reported issues:
--   1. contact_no accepting non-numeric strings
--   2. email accepting comma-separated multiple addresses
-- Plus all other field-level data integrity constraints.
-- Run AFTER 01_schema.sql (tables must exist).
-- MySQL 8.0+ supports CHECK constraints enforced at engine level.
-- ============================================================

USE placement_records_db;

-- ============================================================
-- SECTION 1: Students — phone & email validation
-- ============================================================

-- 1a. phone: must be 10-digit numeric string (Indian mobile number)
--     Rejects: 'abc', '98765', '9876543210x', '+919876543210'
--     Accepts: '9876543210'
ALTER TABLE Students
  ADD CONSTRAINT chk_student_phone
    CHECK (phone REGEXP '^[6-9][0-9]{9}$');
-- Explanation: Indian mobile numbers start with 6–9 followed by 9 more digits.
-- Change to '^[0-9]{10}$' if international or non-Indian numbers are allowed.

-- 1b. email: single valid email address (no commas, no spaces)
--     Rejects: 'a@b.com,c@d.com', 'notanemail', '@nodomain'
--     Accepts: 'hrithik@iiitg.ac.in'
ALTER TABLE Students
  ADD CONSTRAINT chk_student_email
    CHECK (
      email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+){1,2}$'
      AND email NOT LIKE '%,%'
      AND email NOT LIKE '% %'
    );

-- 1c. cgpa: must be between 0.00 and 10.00
ALTER TABLE Students
  ADD CONSTRAINT chk_student_cgpa
    CHECK (cgpa >= 0.00 AND cgpa <= 10.00);

-- 1d. active_backlogs: cannot be negative
ALTER TABLE Students
  ADD CONSTRAINT chk_student_backlogs
    CHECK (active_backlogs >= 0);

-- 1e. batch_year: must be a plausible graduation year (2015–2035)
ALTER TABLE Students
  ADD CONSTRAINT chk_student_batch_year
    CHECK (batch_year >= 2015 AND batch_year <= 2035);

-- 1f. name: must not be blank / whitespace-only
ALTER TABLE Students
  ADD CONSTRAINT chk_student_name_nonempty
    CHECK (TRIM(name) <> '');

-- ============================================================
-- SECTION 2: Companies — website & name validation
-- ============================================================

-- 2a. website: if provided, must start with http:// or https://
ALTER TABLE Companies
  ADD CONSTRAINT chk_company_website
    CHECK (
      website IS NULL
      OR website REGEXP '^https?://.+'
    );

-- 2b. company name must not be blank
ALTER TABLE Companies
  ADD CONSTRAINT chk_company_name_nonempty
    CHECK (TRIM(name) <> '');

-- ============================================================
-- SECTION 3: HR_Contacts — phone & email validation
-- ============================================================

-- 3a. HR contact phone: same 10-digit Indian mobile rule
ALTER TABLE HR_Contacts
  ADD CONSTRAINT chk_hr_phone
    CHECK (
      contact_no IS NULL
      OR contact_no REGEXP '^[6-9][0-9]{9}$'
    );

-- 3b. HR contact email: single valid email, no commas
ALTER TABLE HR_Contacts
  ADD CONSTRAINT chk_hr_email
    CHECK (
      email IS NULL
      OR (
        email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+){1,2}$'
        AND email NOT LIKE '%,%'
        AND email NOT LIKE '% %'
      )
    );

-- ============================================================
-- SECTION 4: Drives — package & date validation
-- ============================================================

-- 4a. package_lpa must be positive if provided
ALTER TABLE Drives
  ADD CONSTRAINT chk_drive_package_positive
    CHECK (package_lpa IS NULL OR package_lpa > 0);

-- 4b. package_lpa sanity cap (no campus drive > 200 LPA is realistic)
ALTER TABLE Drives
  ADD CONSTRAINT chk_drive_package_max
    CHECK (package_lpa IS NULL OR package_lpa <= 200.00);

-- 4c. drive_date must not be in the distant past (before 2010)
ALTER TABLE Drives
  ADD CONSTRAINT chk_drive_date_min
    CHECK (drive_date >= '2010-01-01');

-- ============================================================
-- SECTION 5: DriveEligibility — CGPA & backlog bounds
-- ============================================================

-- 5a. min_cgpa must be between 0 and 10
ALTER TABLE DriveEligibility
  ADD CONSTRAINT chk_eligibility_cgpa
    CHECK (min_cgpa IS NULL OR (min_cgpa >= 0.00 AND min_cgpa <= 10.00));

-- 5b. max_backlogs cannot be negative
ALTER TABLE DriveEligibility
  ADD CONSTRAINT chk_eligibility_backlogs
    CHECK (max_backlogs >= 0);

-- ============================================================
-- SECTION 6: Applications — date and waitlist validation
-- ============================================================

-- 6a. waitlist_rank must be positive if set
ALTER TABLE Applications
  ADD CONSTRAINT chk_app_waitlist_positive
    CHECK (waitlist_rank IS NULL OR waitlist_rank > 0);

-- 6b. offer_letter_date must not precede apply_date
ALTER TABLE Applications
  ADD CONSTRAINT chk_app_offer_after_apply
    CHECK (
      offer_letter_date IS NULL
      OR offer_letter_date >= apply_date
    );

-- ============================================================
-- SECTION 7: Rounds — score validation
-- ============================================================

-- 7a. score must be non-negative if provided
ALTER TABLE Rounds
  ADD CONSTRAINT chk_round_score_nonneg
    CHECK (score IS NULL OR score >= 0);

-- 7b. round_number must be positive (1, 2, 3 ...)
ALTER TABLE Rounds
  ADD CONSTRAINT chk_round_number_positive
    CHECK (round_number > 0);

-- ============================================================
-- Verification: list all CHECK constraints added
-- ============================================================
SELECT
  TABLE_NAME,
  CONSTRAINT_NAME,
  CHECK_CLAUSE
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'placement_records_db'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
