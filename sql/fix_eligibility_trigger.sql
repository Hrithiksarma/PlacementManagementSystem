-- ============================================================
-- PRMS — Fix: Create EligibilityCriteria table + repair trigger
--
-- Problem: The BEFORE INSERT trigger on Applications references
-- EligibilityCriteria table which does not exist in the DB.
-- Also the trigger used dept_id which the Hibernate entity
-- does not have (it uses allowed_departments VARCHAR instead).
--
-- Run this script ONCE in MySQL against placement_records_db.
-- ============================================================

USE placement_records_db;

-- ── Step 1: Create EligibilityCriteria table ─────────────────
-- Schema matches the Hibernate entity EligibilityCriteria.java
-- (criteria_id, drive_id FK, min_cgpa DOUBLE, max_backlogs INT,
--  allowed_tiers VARCHAR, allowed_departments VARCHAR)
CREATE TABLE IF NOT EXISTS EligibilityCriteria (
    criteria_id         INT          PRIMARY KEY AUTO_INCREMENT,
    drive_id            INT          NOT NULL,
    min_cgpa            DOUBLE       NULL,
    max_backlogs        INT          NULL,
    allowed_tiers       VARCHAR(255) NULL,
    allowed_departments VARCHAR(255) NULL,
    CONSTRAINT fk_elig_criteria_drive
        FOREIGN KEY (drive_id) REFERENCES Drives(drive_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ── Step 2: Recreate the trigger without dept_id ─────────────
-- The old trigger queried EligibilityCriteria by dept_id, but
-- the Hibernate entity uses allowed_departments (a string).
-- New trigger: query EligibilityCriteria by drive_id only,
-- fall back to Drives.min_cgpa / max_backlogs if no row found.
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
    DECLARE v_min_cgpa      DOUBLE       DEFAULT NULL;
    DECLARE v_max_backlogs  INT          DEFAULT NULL;
    DECLARE v_cgpa_msg      VARCHAR(200) DEFAULT NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN END;

    IF IFNULL(@DISABLE_TRIGGERS, 0) = 0 THEN
        -- Load student data
        SELECT placement_tier, cgpa, active_backlogs
        INTO   v_student_tier, v_student_cgpa, v_backlogs
        FROM   Students WHERE student_id = NEW.student_id;

        -- Load company tier
        SELECT c.tier INTO v_company_tier
        FROM   Drives d
        JOIN   Companies c ON d.company_id = c.company_id
        WHERE  d.drive_id = NEW.drive_id;

        -- ── Rule (a): Placement-tier restrictions ─────────────
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

        -- ── Rule (b): Academic eligibility ───────────────────
        -- Try EligibilityCriteria first (Hibernate-managed, drive-level override)
        SELECT min_cgpa, max_backlogs
        INTO   v_min_cgpa, v_max_backlogs
        FROM   EligibilityCriteria
        WHERE  drive_id = NEW.drive_id
        LIMIT 1;

        -- Fall back to Drives.min_cgpa / max_backlogs when no criteria row exists
        IF v_min_cgpa IS NULL THEN
            SELECT min_cgpa INTO v_min_cgpa
            FROM   Drives WHERE drive_id = NEW.drive_id;
        END IF;
        IF v_max_backlogs IS NULL THEN
            SELECT max_backlogs INTO v_max_backlogs
            FROM   Drives WHERE drive_id = NEW.drive_id;
        END IF;

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

-- ── Verify ───────────────────────────────────────────────────
SHOW TABLES LIKE 'EligibilityCriteria';
SHOW TRIGGERS FROM placement_records_db LIKE 'prevent_ineligible_apply';
