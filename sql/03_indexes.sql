-- ============================================================
-- PRMS — File 03: Indexes
-- Performance indexes on frequently queried columns.
-- Run AFTER 02_triggers.sql
-- ============================================================

USE placement_records_db;

-- ── Original indexes from design ─────────────────────────────

-- Speeds up eligibility filtering queries
-- Q2, Q8: WHERE cgpa >= X AND dept_id = Y
CREATE INDEX idx_students_dept_cgpa
    ON Students (dept_id, cgpa);

-- Speeds up placement status lookups per student
-- Q1, Q3, Q6, Q7: WHERE student_id = X AND result = 'Selected'
CREATE INDEX idx_applications_student_result
    ON Applications (student_id, result);

-- Speeds up drive schedule lookups
-- Q10: WHERE drive_date BETWEEN ... / ORDER BY drive_date
CREATE INDEX idx_drives_date_status
    ON Drives (drive_date, status);

-- Speeds up round lookups per application
-- Q9: GROUP BY round_type, WHERE application_id = X
CREATE INDEX idx_rounds_app_number
    ON Rounds (application_id, round_number);

-- ── Additional production indexes ────────────────────────────

-- Upcoming-drive dashboard: WHERE status = 'Upcoming' ORDER BY drive_date
CREATE INDEX idx_drives_status_date
    ON Drives (status, drive_date);

-- Placement tier filter for eligibility (most-used WHERE clause)
-- WHERE placement_tier = 'Unplaced' AND cgpa >= X
CREATE INDEX idx_students_tier_cgpa
    ON Students (placement_tier, cgpa);

-- Batch-year cohort reports (YoY comparison)
CREATE INDEX idx_students_batch
    ON Students (batch_year, dept_id);


-- ── Verify ───────────────────────────────────────────────────
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'placement_records_db'
  AND INDEX_NAME <> 'PRIMARY'
GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE
ORDER BY TABLE_NAME, INDEX_NAME;
