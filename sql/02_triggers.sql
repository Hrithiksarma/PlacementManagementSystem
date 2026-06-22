-- ============================================================
-- PRMS — File 02: Triggers (Business Rule Enforcement)
-- 5 business triggers + 1 UPDATE variant = 6 total
-- Run AFTER 01_schema.sql
-- ============================================================

USE placement_records_db;

-- ── TRIGGER 1: after_offer_accepted ──────────────────────────
-- AFTER UPDATE on Applications
-- Fires when status → 'Offer Accepted' (student explicitly accepted).
-- Updates Students.placement_tier only if the new tier outranks the current one.
-- The Java service (StudentPortalServiceImpl.acceptOffer) does the same —
-- both paths are idempotent and belt-and-suspenders for each other.
DROP TRIGGER IF EXISTS after_selection;
DROP TRIGGER IF EXISTS after_offer_accepted;
DELIMITER $$
CREATE TRIGGER after_offer_accepted
AFTER UPDATE ON Applications
FOR EACH ROW
BEGIN
    DECLARE v_company_tier   VARCHAR(20);
    DECLARE v_current_tier   VARCHAR(20);
    DECLARE v_current_rank   INT DEFAULT 0;
    DECLARE v_new_rank       INT DEFAULT 0;

    DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN END;

    IF NEW.status = 'Offer Accepted' AND OLD.status <> 'Offer Accepted' THEN
        SELECT c.tier INTO v_company_tier
        FROM   Drives d
        JOIN   Companies c ON d.company_id = c.company_id
        WHERE  d.drive_id = NEW.drive_id;

        SELECT placement_tier INTO v_current_tier
        FROM   Students WHERE student_id = NEW.student_id;

        -- Map tier names to numeric ranks for comparison
        SET v_current_rank = CASE v_current_tier
            WHEN 'Super Dream' THEN 3
            WHEN 'Dream'       THEN 2
            WHEN 'Normal'      THEN 1
            ELSE 0
        END;
        SET v_new_rank = CASE v_company_tier
            WHEN 'Super Dream' THEN 3
            WHEN 'Dream'       THEN 2
            WHEN 'Normal'      THEN 1
            ELSE 0
        END;

        -- Only upgrade — never downgrade placement_tier
        IF v_new_rank > v_current_rank THEN
            UPDATE Students
            SET    placement_tier = v_company_tier
            WHERE  student_id = NEW.student_id;
        END IF;
    END IF;
END$$
DELIMITER ;


-- ── TRIGGER 2: prevent_ineligible_apply ──────────────────────
-- BEFORE INSERT on Applications
-- Students.placement_tier = Actual Placement Status (set by after_selection).
-- Academic eligibility (CGPA tier) is computed at runtime and is NOT stored.
-- Rejects if:
--   (a) Placement tier blocks this company's tier, OR
--   (b) Student does not meet CGPA/backlog criteria in EligibilityCriteria
DROP TRIGGER IF EXISTS prevent_ineligible_apply;
DELIMITER $$
CREATE TRIGGER prevent_ineligible_apply
BEFORE INSERT ON Applications
FOR EACH ROW
BEGIN
    DECLARE v_student_tier  VARCHAR(20);
    DECLARE v_company_tier  VARCHAR(20) DEFAULT NULL;
    DECLARE v_student_cgpa  DECIMAL(4,2);
    DECLARE v_backlogs      INT;
    DECLARE v_min_cgpa      DECIMAL(4,2) DEFAULT NULL;
    DECLARE v_max_backlogs  INT          DEFAULT NULL;
    DECLARE v_dept_id       INT;
    DECLARE v_cgpa_msg      VARCHAR(200) DEFAULT NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN END;

    IF IFNULL(@DISABLE_TRIGGERS, 0) = 0 THEN
        SELECT placement_tier, cgpa, active_backlogs, dept_id
        INTO   v_student_tier, v_student_cgpa, v_backlogs, v_dept_id
        FROM   Students WHERE student_id = NEW.student_id;

        SELECT c.tier INTO v_company_tier
        FROM   Drives d
        JOIN   Companies c ON d.company_id = c.company_id
        WHERE  d.drive_id = NEW.drive_id;

        -- Rule (a): Tier-based restrictions using actual placement status
        -- Unplaced     → may apply to Normal / Dream / Super Dream
        -- Normal placed → may apply to Dream / Super Dream only
        -- Dream placed  → may apply to Super Dream only
        -- Super Dream placed → may not apply anywhere
        IF v_student_tier = 'Super Dream' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Student already placed at Super Dream tier.';
        END IF;

        IF v_student_tier = 'Dream' AND v_company_tier IN ('Normal', 'Dream') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Dream-placed students can only apply to Super Dream companies.';
        END IF;

        IF v_student_tier = 'Normal' AND v_company_tier = 'Normal' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'INELIGIBLE: Normal-placed students cannot apply to Normal tier drives.';
        END IF;

        -- Rule (b): CGPA and backlog eligibility from EligibilityCriteria
        SELECT min_cgpa, max_backlogs
        INTO   v_min_cgpa, v_max_backlogs
        FROM   EligibilityCriteria
        WHERE  drive_id = NEW.drive_id
          AND  dept_id  = v_dept_id
        LIMIT 1;

        IF v_min_cgpa IS NOT NULL AND v_student_cgpa < v_min_cgpa THEN
            SET v_cgpa_msg = CONCAT('INELIGIBLE: Minimum CGPA required is ', v_min_cgpa, '.');
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_cgpa_msg;
        END IF;

        IF v_max_backlogs IS NOT NULL AND v_backlogs > v_max_backlogs THEN
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
