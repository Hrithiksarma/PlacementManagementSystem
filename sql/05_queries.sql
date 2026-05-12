-- ============================================================
-- PRMS — File 05: Queries
-

-- Run AFTER 04_data.sql
-- ============================================================

USE placement_records_db;

-- ============================================================
-- MANDATORY QUERIES (Q1–Q6)
-- ============================================================

-- ── Q1: Students who applied after January 2025 ─────────────

-- Concepts: JOIN, WHERE with DATE filter, DISTINCT
SELECT DISTINCT
    s.student_id,
    s.name,
    dep.section,
    dep.program,
    a.apply_date
FROM Students s
JOIN Applications a  ON s.student_id = a.student_id
JOIN Departments dep ON s.dept_id    = dep.dept_id
WHERE a.apply_date > '2025-01-31'
ORDER BY a.apply_date;


-- ── Q2: IT-sector companies offering packages above 10 LPA ──

-- Concepts: JOIN, multi-condition WHERE, ORDER BY
SELECT
    c.name       AS company_name,
    c.sector,
    c.tier,
    d.role_offered,
    d.package_lpa
FROM Companies c
JOIN Drives d ON c.company_id = d.company_id
WHERE c.sector = 'IT'
  AND d.package_lpa > 10
ORDER BY d.package_lpa DESC;


-- ── Q3: Students selected in a specific company's drive ─────

-- Concepts: 5-table JOIN, specific filter
SELECT
    s.name          AS student_name,
    dep.section,
    dep.program,
    dr.role_offered,
    dr.package_lpa,
    a.offer_letter_date
FROM Students s
JOIN Applications a   ON s.student_id  = a.student_id
JOIN Drives dr        ON a.drive_id    = dr.drive_id
JOIN Companies c      ON dr.company_id = c.company_id
JOIN Departments dep  ON s.dept_id     = dep.dept_id
WHERE c.name     = 'Google'
  AND a.result   = 'Selected';


-- ── Q4: Company placing highest number of students in 2025 ──

-- Concepts: GROUP BY, COUNT, ORDER BY, LIMIT
SELECT
    c.name          AS company_name,
    c.tier,
    COUNT(*)        AS total_placed
FROM Companies c
JOIN Drives d      ON c.company_id = d.company_id
JOIN Applications a ON d.drive_id  = a.drive_id
WHERE a.result    = 'Selected'
  AND YEAR(d.drive_date) = 2025
GROUP BY c.company_id, c.name, c.tier
ORDER BY total_placed DESC
LIMIT 1;


-- ── Q5: Avg package and placement count per department ───────
-- Hospital equivalent: "Average treatment cost per doctor per month"
-- Concepts: AVG, MIN, MAX, GROUP BY, JOIN
SELECT
    dep.program,
    dep.branch,
    dep.section,
    COUNT(*)                           AS total_placed,
    ROUND(AVG(d.package_lpa), 2)       AS avg_package_lpa,
    MIN(d.package_lpa)                 AS min_package_lpa,
    MAX(d.package_lpa)                 AS max_package_lpa
FROM Departments dep
JOIN Students    s  ON dep.dept_id   = s.dept_id
JOIN Applications a ON s.student_id  = a.student_id
JOIN Drives      d  ON a.drive_id    = d.drive_id
WHERE a.result = 'Selected'
GROUP BY dep.program, dep.branch, dep.section
ORDER BY avg_package_lpa DESC;


-- ── Q6: Highest package offer with student and company details
-- Hospital equivalent: "Most expensive treatment with patient + doctor"
-- Concepts: ORDER BY DESC, LIMIT, multi-table JOIN
SELECT
    s.name          AS student_name,
    dep.section,
    dep.program,
    c.name          AS company_name,
    c.tier,
    dr.role_offered,
    dr.package_lpa,
    a.offer_letter_date
FROM Students s
JOIN Applications a  ON s.student_id  = a.student_id
JOIN Drives dr       ON a.drive_id    = dr.drive_id
JOIN Companies c     ON dr.company_id = c.company_id
JOIN Departments dep ON s.dept_id     = dep.dept_id
WHERE a.result = 'Selected'
ORDER BY dr.package_lpa DESC
LIMIT 1;


-- ============================================================
-- ANALYTICAL QUERIES (Q7–Q10)
-- ============================================================

-- ── Q7: Tier-wise placement distribution per department ──────
-- Concepts: CASE-based conditional aggregation, GROUP BY, percentage
SELECT
    dep.program,
    dep.branch,
    dep.section,
    COUNT(*)                                                              AS total_students,
    COUNT(CASE WHEN s.placement_tier = 'Normal'      THEN 1 END)        AS normal_tier,
    COUNT(CASE WHEN s.placement_tier = 'Dream'       THEN 1 END)        AS dream_tier,
    COUNT(CASE WHEN s.placement_tier = 'Super Dream' THEN 1 END)        AS super_dream_tier,
    COUNT(CASE WHEN s.placement_tier = 'Unplaced'    THEN 1 END)        AS unplaced,
    ROUND(
        COUNT(CASE WHEN s.placement_tier <> 'Unplaced' THEN 1 END)
        * 100.0 / COUNT(*), 2
    )                                                                     AS placement_pct
FROM Students s
JOIN Departments dep ON s.dept_id = dep.dept_id
GROUP BY dep.program, dep.branch, dep.section
ORDER BY dep.program, dep.branch, dep.section;


-- ── Q8: Eligible students for an upcoming drive (excluding placed)
-- Concepts: Multi-table JOIN, NOT EXISTS subquery, tier filtering
-- Change drive_id = 23 to the desired upcoming drive
SELECT
    s.student_id,
    s.name,
    dep.section,
    dep.program,
    s.cgpa,
    s.active_backlogs,
    s.placement_tier,
    de.min_cgpa       AS required_cgpa
FROM Students s
JOIN Departments      dep ON s.dept_id   = dep.dept_id
JOIN DriveEligibility de  ON dep.dept_id = de.dept_id
WHERE de.drive_id           = 23           -- Google Intern (upcoming)
  AND s.cgpa                >= de.min_cgpa
  AND s.active_backlogs     <= de.max_backlogs
  AND s.placement_tier      <> 'Super Dream'   -- Super Dream students ineligible
  AND NOT EXISTS (
        SELECT 1 FROM Applications a
        WHERE a.student_id = s.student_id
          AND a.drive_id   = de.drive_id
      )
ORDER BY s.cgpa DESC;


-- ── Q9: Round-wise elimination analysis ─────────────────────
-- Concepts: Conditional COUNT, percentage calculation, GROUP BY
SELECT
    r.round_type,
    COUNT(*)                                                          AS total_appeared,
    COUNT(CASE WHEN r.status = 'Pass' THEN 1 END)                    AS passed,
    COUNT(CASE WHEN r.status = 'Fail' THEN 1 END)                    AS eliminated,
    COUNT(CASE WHEN r.status = 'Pending' THEN 1 END)                 AS pending,
    ROUND(
        COUNT(CASE WHEN r.status = 'Fail' THEN 1 END) * 100.0
        / COUNT(*), 2
    )                                                                  AS elimination_pct
FROM Rounds r
GROUP BY r.round_type
ORDER BY elimination_pct DESC;


-- ── Q10: Monthly placement funnel ────────────────────────────
-- Concepts: DATE_FORMAT, conditional aggregation, temporal analysis
SELECT
    DATE_FORMAT(d.drive_date, '%Y-%m')     AS month,
    COUNT(DISTINCT d.drive_id)             AS drives_conducted,
    COUNT(a.application_id)               AS total_applications,
    COUNT(CASE WHEN a.result = 'Selected' THEN 1 END) AS selections,
    COUNT(CASE WHEN a.result = 'Rejected' THEN 1 END) AS rejections,
    COUNT(CASE WHEN a.result = 'Pending'  THEN 1 END) AS pending,
    ROUND(
        COUNT(CASE WHEN a.result = 'Selected' THEN 1 END) * 100.0
        / NULLIF(COUNT(a.application_id), 0), 2
    )                                                                   AS selection_rate_pct
FROM Drives d
JOIN Applications a ON d.drive_id = a.drive_id
GROUP BY DATE_FORMAT(d.drive_date, '%Y-%m')
ORDER BY month;
