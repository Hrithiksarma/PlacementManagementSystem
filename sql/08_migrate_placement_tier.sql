-- ============================================================
-- PRMS — File 08: Placement Tier Migration
-- Redefines Students.placement_tier as Actual Placement Status.
-- Run ONCE after 02_triggers.sql is applied.
-- ============================================================

USE placement_records_db;

-- Step 1: Reset all students to Unplaced.
--         placement_tier now means "placed at this company tier", not CGPA eligibility.
UPDATE Students SET placement_tier = 'Unplaced';

-- Step 2: Backfill from existing Applications where status = 'Selected'.
--         For students with multiple selected offers (rare), the highest tier wins.
--         The after_selection trigger handles this going forward automatically.
UPDATE Students s
INNER JOIN (
    SELECT
        a.student_id,
        CASE MAX(CASE c.tier
                     WHEN 'Super Dream' THEN 3
                     WHEN 'Dream'       THEN 2
                     WHEN 'Normal'      THEN 1
                     ELSE 0
                 END)
            WHEN 3 THEN 'Super Dream'
            WHEN 2 THEN 'Dream'
            WHEN 1 THEN 'Normal'
            ELSE        'Unplaced'
        END AS highest_tier
    FROM  Applications a
    JOIN  Drives   d ON a.drive_id   = d.drive_id
    JOIN  Companies c ON d.company_id = c.company_id
    WHERE a.status = 'Selected'
    GROUP BY a.student_id
) ranked ON s.student_id = ranked.student_id
SET s.placement_tier = ranked.highest_tier
WHERE ranked.highest_tier <> 'Unplaced';

-- Verify
SELECT student_id, name, cgpa, placement_tier FROM Students ORDER BY name;
