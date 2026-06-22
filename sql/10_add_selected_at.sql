-- ============================================================
-- PRMS — File 10: Add selected_at column
-- Purpose: track the exact timestamp when an application became
-- Selected, so the placement offers API can sort best-offer-first
-- (tier DESC, then most-recent-first within the same tier).
--
-- Hibernate ddl-auto=update adds the selected_at column on startup.
-- This script backfills it for existing Selected rows using updated_at.
--
-- Previous offers (students selected at HCL Normal, then at Microsoft
-- Super Dream) stay as-is in the Selected state — the UI shows them
-- as "Previous Offer" cards behind the best offer in the carousel.
--
-- Run ONCE after deploying the updated backend, then restart.
-- ============================================================

USE placement_records_db;

-- Step 1: Add selected_at column if it doesn't exist yet.
-- (Hibernate may have already added it on startup — ALTER ignores that.)
ALTER TABLE Applications
    ADD COLUMN IF NOT EXISTS selected_at DATETIME NULL;

-- Step 2: Backfill selected_at from updated_at for existing Selected rows.
-- These rows were Selected before this column existed, so updated_at is
-- the best available proxy for when the offer was confirmed.
UPDATE Applications
SET selected_at = COALESCE(updated_at, application_date)
WHERE status = 'Selected'
  AND selected_at IS NULL;

-- Step 3: Verify — check how many students have multiple Selected offers.
-- These are intentional (e.g. HCL Normal + Microsoft Super Dream) and will
-- appear as "Previous Offer" cards in the student's placement history.
SELECT
    student_id,
    COUNT(*) AS offer_count,
    GROUP_CONCAT(application_id ORDER BY selected_at DESC) AS offer_ids
FROM Applications
WHERE status = 'Selected'
GROUP BY student_id
HAVING COUNT(*) > 1;

-- Step 4: Overview of final state.
SELECT
    status,
    COUNT(*) AS total
FROM Applications
GROUP BY status
ORDER BY status;
