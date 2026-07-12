-- ============================================================
-- PRMS — File 13: Add roll_no to Students
-- Format: [YY][DDD][NN] — 7 digits, fully numeric
--   YY  = last 2 digits of batch_year
--   DDD = 3-digit dept code (see mapping below)
--   NN  = 2-digit serial within dept + batch year (01-99)
--
-- Dept code mapping:
--   111 = B.Tech CSE C13  |  112 = B.Tech CSE C14
--   121 = B.Tech ECE E13  |  122 = B.Tech ECE E14
--   211 = M.Tech CSE M13  |  212 = M.Tech CSE M14
--   221 = M.Tech ECE ME13 |  222 = M.Tech ECE ME14
--
-- Steps:
--   1. Add nullable column (needed to backfill before NOT NULL)
--   2. Backfill all existing students via window function
--   3. Tighten to NOT NULL
--   4. Create BEFORE INSERT trigger — fires for every new
--      student regardless of how they are added (SQL, API, or
--      DataInitializer).  Seed files 11 & 12 need no changes.
--
-- Requires MySQL 8.0+ (ROW_NUMBER window function).
-- Run AFTER 04_data.sql (and optionally 11 and 12).
-- ============================================================

USE placement_records_db;

-- ── Step 1: Add column (nullable for now) ────────────────────
ALTER TABLE Students
    ADD COLUMN roll_no CHAR(7) NULL UNIQUE
    AFTER student_id;

-- ── Step 2: Backfill existing students ───────────────────────
-- ROW_NUMBER() restarts at 1 for each (dept_id, batch_year) group,
-- ordered by student_id so earlier-seeded students get lower serials.
UPDATE Students s
JOIN (
    SELECT
        s2.student_id,
        CONCAT(
            RIGHT(s2.batch_year, 2),
            CASE s2.dept_id
                WHEN 1 THEN '111'
                WHEN 2 THEN '112'
                WHEN 3 THEN '121'
                WHEN 4 THEN '122'
                WHEN 5 THEN '211'
                WHEN 6 THEN '212'
                WHEN 7 THEN '221'
                WHEN 8 THEN '222'
                ELSE LPAD(s2.dept_id, 3, '0')   -- future-proof fallback
            END,
            LPAD(
                ROW_NUMBER() OVER (
                    PARTITION BY s2.dept_id, s2.batch_year
                    ORDER BY     s2.student_id
                ),
                2, '0'
            )
        ) AS roll
    FROM Students s2
) sub ON s.student_id = sub.student_id
SET s.roll_no = sub.roll;

-- ── Step 3: Tighten to NOT NULL ──────────────────────────────
ALTER TABLE Students
    MODIFY COLUMN roll_no CHAR(7) NOT NULL;

-- ── Step 4: Trigger for future INSERTs ───────────────────────
DROP TRIGGER IF EXISTS trg_assign_roll_no;

DELIMITER //
CREATE TRIGGER trg_assign_roll_no
BEFORE INSERT ON Students
FOR EACH ROW
BEGIN
    DECLARE v_dept_code CHAR(3);
    DECLARE v_serial    INT;

    -- Map dept_id → 3-digit code
    SET v_dept_code = CASE NEW.dept_id
        WHEN 1 THEN '111'
        WHEN 2 THEN '112'
        WHEN 3 THEN '121'
        WHEN 4 THEN '122'
        WHEN 5 THEN '211'
        WHEN 6 THEN '212'
        WHEN 7 THEN '221'
        WHEN 8 THEN '222'
        ELSE LPAD(NEW.dept_id, 3, '0')
    END;

    -- Count existing students in this dept + batch year to get next serial
    SELECT COUNT(*) + 1 INTO v_serial
    FROM   Students
    WHERE  dept_id   = NEW.dept_id
    AND    batch_year = NEW.batch_year;

    -- Compose the 7-digit roll number
    SET NEW.roll_no = CONCAT(
        RIGHT(NEW.batch_year, 2),
        v_dept_code,
        LPAD(v_serial, 2, '0')
    );
END //
DELIMITER ;

-- ── Verify ───────────────────────────────────────────────────
SELECT
    student_id,
    roll_no,
    name,
    batch_year,
    dept_id
FROM Students
ORDER BY dept_id, batch_year, student_id
LIMIT 20;