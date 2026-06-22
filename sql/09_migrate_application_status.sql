-- ============================================================
-- PRMS — File 09: Application Status Column Migration
-- Root cause: original schema stored workflow state in
--   Applications.result ENUM('Pending','Selected','Rejected').
-- Hibernate ddl-auto=update added Applications.status VARCHAR
-- for the new 5-stage workflow, but left old rows with status=NULL.
-- This script backfills status from result for those old rows.
--
-- Run ONCE, then restart the backend.
-- ============================================================

USE placement_records_db;

-- Step 1: Backfill status from result for rows where status is missing.
-- Mapping:  Pending  → Applied   (no offer yet)
--           Selected → Selected  (preserve final state)
--           Rejected → Rejected  (preserve final state)
UPDATE Applications
SET status = CASE
    WHEN result = 'Selected'                      THEN 'Selected'
    WHEN result = 'Rejected'                      THEN 'Rejected'
    ELSE 'Applied'
END
WHERE status IS NULL OR TRIM(status) = '' OR status = 'Pending';

-- Step 2: Ensure no remaining NULL/empty/Pending rows.
SELECT
    application_id,
    student_id,
    drive_id,
    result   AS old_result,
    status   AS new_status
FROM Applications
WHERE status IS NULL
   OR TRIM(status) = ''
   OR status = 'Pending';
-- Expected: Empty Result Set

-- Step 3: Make status column NOT NULL with a default.
-- Skip if already applied.
ALTER TABLE Applications
    MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'Applied';

-- Verify final state.
SELECT
    status,
    COUNT(*) AS total
FROM Applications
GROUP BY status
ORDER BY status;
