-- ============================================================
-- PRMS — File 06: Trigger Verification Tests
-- Each test has an expected outcome noted in a comment.
-- Run AFTER 04_data.sql  (data must be loaded first)
-- ============================================================

USE placement_records_db;

-- CRITICAL: Ensure triggers are fully enabled for tests!
SET @DISABLE_TRIGGERS = 0;

-- ============================================================
-- TEST 1 — Trigger: after_selection + auto_offer_date
-- Action : Update an application result to 'Selected'
-- Expect : Student's placement_tier updates; offer_letter_date set
-- ============================================================

-- Before state
SELECT student_id, name, placement_tier FROM Students WHERE student_id = 9;
-- Ishaan Gupta should currently be 'Unplaced'

SELECT application_id, result, offer_letter_date
FROM Applications WHERE application_id = 24;
-- Drive 12 (Flipkart) app — currently 'Selected' was set in data

-- Manual test: update a Pending app to Selected
UPDATE Applications
SET result = 'Selected'
WHERE application_id = 35   -- Walmart, Pending
  AND result = 'Pending';

-- After state: student 34's tier should now match Walmart's tier (Dream)
SELECT s.student_id, s.name, s.placement_tier
FROM Students s WHERE s.student_id = (
    SELECT student_id FROM Applications WHERE application_id = 35
);

-- offer_letter_date should now be today
SELECT application_id, result, offer_letter_date
FROM Applications WHERE application_id = 35;

-- ============================================================
-- TEST 2 — Trigger: auto_reject_on_fail
-- Action : Mark a Pending round as 'Fail'
-- Expect : Parent application result changes to 'Rejected'
-- ============================================================

-- Choose a Pending application
SELECT application_id, result FROM Applications WHERE application_id = 58;
-- Wipro 2025, result = 'Pending'

SELECT round_id, round_number, round_type, status
FROM Rounds WHERE application_id = 58;

-- Fail the round
UPDATE Rounds
SET status = 'Fail'
WHERE application_id = 58 AND round_number = 1;

-- After state: application 58 should now be 'Rejected'
SELECT application_id, result FROM Applications WHERE application_id = 58;


-- ============================================================
-- TEST 3 — Trigger: prevent_ineligible_apply
-- Sub-test A: Super Dream student tries to apply
-- Expect: ERROR 45000 — already placed at Super Dream tier
-- ============================================================
-- student_id=1 (Aarav, Super Dream), drive_id=12 (Flipkart FTE, Dream)
-- This INSERT should FAIL:
INSERT INTO Applications (student_id, drive_id, apply_date, result)
VALUES (1, 12, CURDATE(), 'Pending');
-- Expected: ERROR 1644 (SIGNAL): INELIGIBLE: Student already placed at Super Dream tier.


-- ============================================================
-- TEST 3 — Sub-test B: Normal-placed student applies to Normal drive
-- Expect: ERROR 45000
-- ============================================================
-- student_id=3 (Chirag, Normal), drive_id=19 (Infosys, Normal tier)
-- Chirag is already Normal-placed — should FAIL:
INSERT INTO Applications (student_id, drive_id, apply_date, result)
VALUES (3, 19, CURDATE(), 'Pending');
-- Expected: ERROR 1644 (SIGNAL): INELIGIBLE: Normal-placed student cannot apply...


-- ============================================================
-- TEST 3 — Sub-test C: Student below CGPA cutoff
-- Expect: ERROR 45000
-- ============================================================
-- student_id=11 (Kiran Bose, cgpa=6.80, dept C14), drive_id=8 (Google, min_cgpa=8.00)
INSERT INTO Applications (student_id, drive_id, apply_date, result)
VALUES (11, 8, CURDATE(), 'Pending');
-- Expected: ERROR 1644 (SIGNAL): INELIGIBLE: Student CGPA does not meet minimum...


-- ============================================================
-- TEST 3 — Sub-test D: Student with too many backlogs
-- Expect: ERROR 45000
-- ============================================================
-- student_id=22 (Vasudha, 2 backlogs), drive_id=9 (Microsoft, max_backlogs=0)
INSERT INTO Applications (student_id, drive_id, apply_date, result)
VALUES (22, 9, CURDATE(), 'Pending');
-- Expected: ERROR 1644 (SIGNAL): INELIGIBLE: Student has too many active backlogs...


-- ============================================================
-- TEST 4 — Trigger: validate_hr_company
-- Action : Assign an HR from a different company to a drive
-- Expect : ERROR 45000
-- ============================================================
-- hr_id=1 belongs to Google (company_id=1)
-- Trying to assign to Wipro's drive (company_id=7) should FAIL:
UPDATE Drives SET hr_id = 1 WHERE drive_id = 2;
-- Expected: ERROR 1644 (SIGNAL): INVALID: HR contact does not belong to the specified company.


-- ============================================================
-- TEST 5 — CHECK Constraint: Phone validation (from 07_data_validation.sql)
-- Expect: ERROR 3819 (Check constraint violated)
-- ============================================================
INSERT INTO Students (name, dept_id, batch_year, cgpa, email, phone)
VALUES ('Test Student', 1, 2025, 7.50, 'test.student@iiitg.ac.in', 'abc1234567');
-- Expected: ERROR 3819: Check constraint 'chk_student_phone' is violated.


-- ============================================================
-- TEST 6 — CHECK Constraint: Email with comma
-- Expect: ERROR 3819
-- ============================================================
INSERT INTO Students (name, dept_id, batch_year, cgpa, email)
VALUES ('Test Student2', 1, 2025, 7.50, 'a@b.com,c@d.com');
-- Expected: ERROR 3819: Check constraint 'chk_student_email' is violated.


-- ============================================================
-- TEST 7 — CHECK Constraint: CGPA out of range
-- Expect: ERROR 3819
-- ============================================================
INSERT INTO Students (name, dept_id, batch_year, cgpa, email)
VALUES ('Test Student3', 1, 2025, 11.00, 'test3@iiitg.ac.in');
-- Expected: ERROR 3819: Check constraint 'chk_student_cgpa' is violated.


-- ============================================================
-- TEST 8 — Stored Procedure: sp_register_student
-- Expect: duplicate email blocked with OUT param error message
-- ============================================================
CALL sp_register_student(
    'Duplicate User',
    1,
    2025,
    8.00,
    'aarav.sharma@iiitg.ac.in',   -- already exists
    '9876599999',
    @out_id,
    @out_msg
);
SELECT @out_id AS new_id, @out_msg AS message;
-- Expected: new_id = -1, message = 'ERROR: Email already registered...'


-- ============================================================
-- TEST 9 — Stored Procedure: sp_submit_application idempotency
-- Expect: second application to same drive blocked
-- ============================================================
CALL sp_submit_application(1, 8, @app_id, @msg);
SELECT @app_id AS application_id, @msg AS message;
-- Expected: application_id = -1, message = 'ERROR: Student has already applied...'


-- ============================================================
-- Summary of expected results
-- ============================================================
SELECT
    'Test 1'  AS test, 'Trigger after_selection + auto_offer_date'   AS description, 'placement_tier + offer_letter_date updated'   AS expected UNION ALL
SELECT 'Test 2', 'Trigger auto_reject_on_fail',                                       'Application result changed to Rejected'       UNION ALL
SELECT 'Test 3a','Trigger prevent_ineligible_apply (Super Dream)',                    'ERROR 1644: Super Dream already placed'       UNION ALL
SELECT 'Test 3b','Trigger prevent_ineligible_apply (Normal→Normal)',                  'ERROR 1644: Normal cannot apply Normal'       UNION ALL
SELECT 'Test 3c','Trigger prevent_ineligible_apply (CGPA)',                           'ERROR 1644: CGPA below minimum'              UNION ALL
SELECT 'Test 3d','Trigger prevent_ineligible_apply (Backlogs)',                       'ERROR 1644: Too many backlogs'               UNION ALL
SELECT 'Test 4', 'Trigger validate_hr_company',                                       'ERROR 1644: HR not from company'             UNION ALL
SELECT 'Test 5', 'CHECK chk_student_phone',                                           'ERROR 3819: phone format invalid'            UNION ALL
SELECT 'Test 6', 'CHECK chk_student_email (comma)',                                   'ERROR 3819: multiple emails rejected'        UNION ALL
SELECT 'Test 7', 'CHECK chk_student_cgpa',                                            'ERROR 3819: CGPA > 10.00 rejected'           UNION ALL
SELECT 'Test 8', 'SP sp_register_student duplicate email',                            'OUT: -1, error message returned'            UNION ALL
SELECT 'Test 9', 'SP sp_submit_application duplicate application',                    'OUT: -1, error message returned';
