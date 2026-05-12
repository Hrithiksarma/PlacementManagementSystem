-- ============================================================
-- PRMS — File 02: Triggers (Business Rule Enforcement)
-- 5 business triggers + 1 UPDATE variant = 6 total
-- Run AFTER 01_schema.sql
-- ============================================================

USE placement_records_db;

-- ── TRIGGER 1: after_selection ───────────────────────────────
-- AFTER UPDATE on Applications
-- When result changes to 'Selected', auto-promote student's
-- placement_tier to match the company's tier for that drive.
DROP TRIGGER IF EXISTS after_selection;
DELIMITER $$
CREATE TRIGGER after_selection
AFTER UPDATE ON Applications
FOR EACH ROW
BEGIN
    DECLARE v_company_tier ENUM('Normal','Dream','Super Dream');

    IF NEW.result = 'Selected' AND OLD.result <> 'Selected' THEN
        -- Fetch the tier of the company associated with this drive
        SELECT c.tier INTO v_company_tier
        FROM Drives d
        JOIN Companies c ON d.company_id = c.company_id
        WHERE d.drive_id = NEW.drive_id;

        UPDATE Students
        SET placement_tier = v_company_tier
        WHERE student_id = NEW.student_id;
    END IF;
END$$
DELIMITER ;


-- ── TRIGGER 2: prevent_ineligible_apply ──────────────────────
-- BEFORE INSERT on Applications
-- Rejects if:
--   (a) Student is already placed at same or higher tier, OR
--   (b) Student does not meet CGPA/backlog criteria in DriveEligibility
DROP TRIGGER IF EXISTS prevent_ineligible_apply;
DELIMITER $$
CREATE TRIGGER prevent_ineligible_apply
BEFORE INSERT ON Applications
FOR EACH ROW
BEGIN
    DECLARE v_student_tier  ENUM('Unplaced','Normal','Dream','Super Dream');
    DECLARE v_company_tier  ENUM('Normal','Dream','Super Dream');
    DECLARE v_student_cgpa  DECIMAL(4,2);
    DECLARE v_backlogs      INT;
    DECLARE v_min_cgpa      DECIMAL(4,2);
    DECLARE v_max_backlogs  INT;
    DECLARE v_dept_id       INT;

    IF IFNULL(@DISABLE_TRIGGERS, 0) = 0 THEN
        -- Fetch student attributes
        SELECT placement_tier, cgpa, active_backlogs, dept_id
        INTO v_student_tier, v_student_cgpa, v_backlogs, v_dept_id
        FROM Students WHERE student_id = NEW.student_id;

        -- Fetch company tier for this drive
        SELECT c.tier INTO v_company_tier
        FROM Drives d
        JOIN Companies c ON d.company_id = c.company_id
        WHERE d.drive_id = NEW.drive_id;

        -- Rule (a): Tier-based placement policy
        -- Unplaced  → can apply anywhere
        -- Normal    → can apply Dream / Super Dream only
        -- Dream     → can apply Super Dream only
        -- Super Dream → cannot apply anywhere
        IF v_student_tier = 'Super Dream' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Student already placed at Super Dream tier.';
        END IF;

        IF v_student_tier = 'Dream' AND v_company_tier = 'Normal' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Dream-placed student cannot apply to Normal tier drives.';
        END IF;

        IF v_student_tier = 'Dream' AND v_company_tier = 'Dream' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Dream-placed student cannot reapply to Dream tier drives.';
        END IF;

        IF v_student_tier = 'Normal' AND v_company_tier = 'Normal' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Normal-placed student cannot apply to Normal tier drives.';
        END IF;

        -- Rule (b): CGPA and backlog eligibility
        SELECT min_cgpa, max_backlogs
        INTO v_min_cgpa, v_max_backlogs
        FROM DriveEligibility
        WHERE drive_id = NEW.drive_id AND dept_id = v_dept_id;

        IF v_student_cgpa < v_min_cgpa THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Student CGPA does not meet the minimum requirement for this drive.';
        END IF;

        IF v_backlogs > v_max_backlogs THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Student has too many active backlogs for this drive.';
        END IF;
    END IF;
END$$
DELIMITER ;

/*
-- ── TRIGGER 3: validate_hr_company ───────────────────────────
-- BEFORE INSERT and BEFORE UPDATE on Drives
-- Rejects if the assigned HR contact does not belong to the
-- drive's company (prevents data entry mistakes).
DROP TRIGGER IF EXISTS validate_hr_company_insert;
DROP TRIGGER IF EXISTS validate_hr_company_update;
DELIMITER $$
CREATE TRIGGER validate_hr_company_insert
BEFORE INSERT ON Drives
FOR EACH ROW
BEGIN
    DECLARE v_hr_company INT;
    IF NEW.hr_id IS NOT NULL THEN
        SELECT company_id INTO v_hr_company
        FROM HR_Contacts WHERE hr_id = NEW.hr_id;
        IF v_hr_company <> NEW.company_id THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INVALID: HR contact does not belong to the specified company.';
        END IF;
    END IF;
END$$

CREATE TRIGGER validate_hr_company_update
BEFORE UPDATE ON Drives
FOR EACH ROW
BEGIN
    DECLARE v_hr_company INT;
    IF NEW.hr_id IS NOT NULL THEN
        SELECT company_id INTO v_hr_company
        FROM HR_Contacts WHERE hr_id = NEW.hr_id;
        IF v_hr_company <> NEW.company_id THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INVALID: HR contact does not belong to the specified company.';
        END IF;
    END IF;
END$$
DELIMITER ;


-- ── TRIGGER 4: auto_offer_date ────────────────────────────────
-- BEFORE UPDATE on Applications
-- Automatically sets offer_letter_date to CURDATE() when
-- result is changed to 'Selected' and date is not already set.
DROP TRIGGER IF EXISTS auto_offer_date;
DELIMITER $$
CREATE TRIGGER auto_offer_date
BEFORE UPDATE ON Applications
FOR EACH ROW
BEGIN
    IF NEW.result = 'Selected' AND OLD.result <> 'Selected'
       AND NEW.offer_letter_date IS NULL THEN
        SET NEW.offer_letter_date = CURDATE();
    END IF;
END$$
DELIMITER ;
*/


-- ── TRIGGER 5: auto_reject_on_fail_update ───────────────────────────
-- AFTER UPDATE on Rounds
-- When any round's status changes to 'Fail', automatically
-- marks the parent application result as 'Rejected'.
DROP TRIGGER IF EXISTS auto_reject_on_fail_update;
DELIMITER $$
CREATE TRIGGER auto_reject_on_fail_update
AFTER UPDATE ON Rounds
FOR EACH ROW
BEGIN
    IF NEW.status = 'Fail' AND OLD.status <> 'Fail' THEN
        UPDATE Applications
        SET result = 'Rejected'
        WHERE application_id = NEW.application_id
          AND result = 'Pending';
    END IF;
END$$
DELIMITER ;

-- ── TRIGGER 6: auto_reject_on_fail_insert ───────────────────────────
-- AFTER INSERT on Rounds
-- When any round is added directly with status 'Fail', automatically
-- marks the parent application result as 'Rejected'.
DROP TRIGGER IF EXISTS auto_reject_on_fail_insert;
DELIMITER $$
CREATE TRIGGER auto_reject_on_fail_insert
AFTER INSERT ON Rounds
FOR EACH ROW
BEGIN
    IF NEW.status = 'Fail' THEN
        UPDATE Applications
        SET result = 'Rejected'
        WHERE application_id = NEW.application_id
          AND result = 'Pending';
    END IF;
END$$
DELIMITER ;


-- ── Verify ───────────────────────────────────────────────────
SHOW TRIGGERS FROM placement_records_db;
