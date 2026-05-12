-- ============================================================
-- PRMS — File 04: Test Data
-- Realistic dataset for IIIT Guwahati placement system.
--   8 Departments | 45 Students | 18 Companies | 15 HR Contacts
--  25 Drives | 90+ Eligibility Rules | 70 Applications | 160+ Rounds
-- Triggers are NOT fired during data load (INSERT bypasses
-- prevent_ineligible_apply via SET @DISABLE_TRIGGERS = 1).
-- Run AFTER 03_indexes.sql
-- ============================================================

USE placement_records_db;

-- Disable eligibility trigger during bulk data load
SET @DISABLE_TRIGGERS = 1;

-- Clean slate for multiple runs
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Rounds;
TRUNCATE TABLE Applications;
TRUNCATE TABLE DriveEligibility;
TRUNCATE TABLE Drives;
TRUNCATE TABLE HR_Contacts;
TRUNCATE TABLE Companies;
TRUNCATE TABLE Students;
TRUNCATE TABLE Departments;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- TABLE 1: Departments (8 rows)
-- ============================================================
INSERT INTO Departments (dept_name, program, branch, section) VALUES
('Computer Science & Engineering', 'B.Tech', 'CSE', 'C13'),
('Computer Science & Engineering', 'B.Tech', 'CSE', 'C14'),
('Electronics & Communication Engineering', 'B.Tech', 'ECE', 'E13'),
('Electronics & Communication Engineering', 'B.Tech', 'ECE', 'E14'),
('Computer Science & Engineering', 'M.Tech', 'CSE', 'M13'),
('Computer Science & Engineering', 'M.Tech', 'CSE', 'M14'),
('Electronics & Communication Engineering', 'M.Tech', 'ECE', 'ME13'),
('Electronics & Communication Engineering', 'M.Tech', 'ECE', 'ME14');

-- ============================================================
-- TABLE 2: Companies (18 rows: 10 IT, 2 Consulting, 3 Core,
--                              2 Finance, 1 Fintech)
-- ============================================================
INSERT INTO Companies (name, sector, tier, website) VALUES
('Google',               'IT',          'Super Dream', 'https://careers.google.com'),
('Microsoft',            'IT',          'Super Dream', 'https://careers.microsoft.com'),
('Amazon',               'IT',          'Super Dream', 'https://amazon.jobs'),
('Flipkart',             'IT',          'Dream',       'https://www.flipkart.com/careers'),
('Walmart Global Tech',  'IT',          'Dream',       'https://careers.walmart.com'),
('Infosys',              'IT',          'Normal',      'https://www.infosys.com/careers'),
('Wipro',                'IT',          'Normal',      'https://careers.wipro.com'),
('HCL Technologies',     'IT',          'Normal',      'https://www.hcltech.com/careers'),
('TCS',                  'IT',          'Normal',      'https://www.tcs.com/careers'),
('Persistent Systems',   'IT',          'Normal',      'https://www.persistent.com/careers'),
('McKinsey & Company',   'Consulting',  'Super Dream', 'https://www.mckinsey.com/careers'),
('Deloitte',             'Consulting',  'Dream',       'https://www2.deloitte.com/careers'),
('Samsung R&D',          'Core',        'Dream',       'https://samsung.com/global/careers'),
('Texas Instruments',    'Core',        'Dream',       'https://ti.com/careers'),
('Siemens',              'Core',        'Normal',      'https://jobs.siemens.com'),
('Goldman Sachs',        'Finance',     'Super Dream', 'https://goldmansachs.com/careers'),
('Morgan Stanley',       'Finance',     'Dream',       'https://morganstanley.com/careers'),
('Razorpay',             'Fintech',     'Dream',       'https://razorpay.com/jobs');

-- ============================================================
-- TABLE 3: HR_Contacts (15 rows)
-- ============================================================
INSERT INTO HR_Contacts (company_id, name, contact_no, email) VALUES
(1,  'Ananya Sharma',   '9876543210', 'ananya.sharma@google.com'),
(2,  'Rohan Mehta',     '9812345678', 'rohan.mehta@microsoft.com'),
(3,  'Priya Nair',      '9823456789', 'priya.nair@amazon.com'),
(4,  'Kiran Rao',       '9834567890', 'kiran.rao@flipkart.com'),
(5,  'Sunita Verma',    '9845678901', 'sunita.verma@walmart.com'),
(6,  'Deepak Joshi',    '9856789012', 'deepak.joshi@infosys.com'),
(7,  'Meena Patel',     '9867890123', 'meena.patel@wipro.com'),
(8,  'Arjun Singh',     '9878901234', 'arjun.singh@hcl.com'),
(9,  'Lalitha Krishnan','9889012345', 'lalitha.k@tcs.com'),
(10, 'Saurabh Gupta',   '9890123456', 'saurabh.gupta@persistent.com'),
(11, 'Natasha Iyer',    '9801234567', 'natasha.iyer@mckinsey.com'),
(13, 'Vikram Bose',     '9712345678', 'vikram.bose@samsung.com'),
(14, 'Pooja Reddy',     '9723456789', 'pooja.reddy@ti.com'),
(16, 'Amit Kapoor',     '9734567890', 'amit.kapoor@gs.com'),
(18, 'Divya Menon',     '9745678901', 'divya.menon@razorpay.com');

-- ============================================================
-- TABLE 4: Students (45 rows)
-- 41 from 2025 batch + 4 from 2024 batch (YoY comparison)
-- ============================================================

-- B.Tech CSE C13 (dept_id = 1) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Aarav Sharma',        1, 2025, 9.12, 0, '9876500001', 'aarav.sharma@iiitg.ac.in',    'Super Dream'),
('Bhavna Patel',        1, 2025, 8.75, 0, '9876500002', 'bhavna.patel@iiitg.ac.in',    'Dream'),
('Chirag Mehta',        1, 2025, 8.30, 0, '9876500003', 'chirag.mehta@iiitg.ac.in',    'Normal'),
('Disha Nair',          1, 2025, 7.90, 0, '9876500004', 'disha.nair@iiitg.ac.in',      'Unplaced'),
('Eshan Verma',         1, 2025, 7.50, 1, '9876500005', 'eshan.verma@iiitg.ac.in',     'Unplaced'),
('Falguni Joshi',       1, 2025, 9.40, 0, '9876500006', 'falguni.joshi@iiitg.ac.in',   'Super Dream');

-- B.Tech CSE C14 (dept_id = 2) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Gaurav Singh',        2, 2025, 8.85, 0, '9876500007', 'gaurav.singh@iiitg.ac.in',    'Dream'),
('Harini Krishnan',     2, 2025, 8.20, 0, '9876500008', 'harini.k@iiitg.ac.in',        'Normal'),
('Ishaan Gupta',        2, 2025, 7.65, 0, '9876500009', 'ishaan.gupta@iiitg.ac.in',    'Unplaced'),
('Jaya Reddy',          2, 2025, 9.05, 0, '9876500010', 'jaya.reddy@iiitg.ac.in',      'Super Dream'),
('Kiran Bose',          2, 2025, 6.80, 2, '9876500011', 'kiran.bose@iiitg.ac.in',      'Unplaced'),
('Lavanya Iyer',        2, 2025, 8.55, 0, '9876500012', 'lavanya.iyer@iiitg.ac.in',    'Dream');

-- B.Tech ECE E13 (dept_id = 3) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Manan Kapoor',        3, 2025, 8.10, 0, '9876500013', 'manan.kapoor@iiitg.ac.in',    'Normal'),
('Nidhi Menon',         3, 2025, 7.70, 0, '9876500014', 'nidhi.menon@iiitg.ac.in',     'Unplaced'),
('Om Prakash',          3, 2025, 7.30, 1, '9876500015', 'om.prakash@iiitg.ac.in',      'Unplaced'),
('Pallavi Rao',         3, 2025, 8.90, 0, '9876500016', 'pallavi.rao@iiitg.ac.in',     'Dream'),
('Qasim Siddiqui',      3, 2025, 9.20, 0, '9876500017', 'qasim.siddiqui@iiitg.ac.in', 'Super Dream'),
('Riddhi Shah',         3, 2025, 6.50, 0, '9876500018', 'riddhi.shah@iiitg.ac.in',     'Normal');

-- B.Tech ECE E14 (dept_id = 4) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Sahil Thakur',        4, 2025, 8.40, 0, '9876500019', 'sahil.thakur@iiitg.ac.in',    'Normal'),
('Tanvi Desai',         4, 2025, 7.80, 0, '9876500020', 'tanvi.desai@iiitg.ac.in',     'Unplaced'),
('Uday Banerjee',       4, 2025, 9.00, 0, '9876500021', 'uday.banerjee@iiitg.ac.in',   'Dream'),
('Vasudha Chandra',     4, 2025, 7.20, 2, '9876500022', 'vasudha.chandra@iiitg.ac.in', 'Unplaced'),
('Waqar Ali',           4, 2025, 8.60, 0, '9876500023', 'waqar.ali@iiitg.ac.in',       'Dream'),
('Xena Pillai',         4, 2025, 6.90, 0, '9876500024', 'xena.pillai@iiitg.ac.in',     'Normal');

-- M.Tech CSE M13 (dept_id = 5) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Yash Kumar',          5, 2025, 8.70, 0, '9876500025', 'yash.kumar@iiitg.ac.in',      'Dream'),
('Zara Hussain',        5, 2025, 9.30, 0, '9876500026', 'zara.hussain@iiitg.ac.in',    'Super Dream'),
('Abhinav Tiwari',      5, 2025, 7.95, 0, '9876500027', 'abhinav.tiwari@iiitg.ac.in',  'Normal'),
('Bharat Mishra',       5, 2025, 8.15, 0, '9876500028', 'bharat.mishra@iiitg.ac.in',   'Normal'),
('Chandni Agrawal',     5, 2025, 7.40, 1, '9876500029', 'chandni.agrawal@iiitg.ac.in', 'Unplaced');

-- M.Tech CSE M14 (dept_id = 6) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Dinesh Yadav',        6, 2025, 8.80, 0, '9876500030', 'dinesh.yadav@iiitg.ac.in',    'Dream'),
('Ekta Srivastava',     6, 2025, 9.10, 0, '9876500031', 'ekta.srivastava@iiitg.ac.in', 'Super Dream'),
('Farhan Sheikh',       6, 2025, 7.60, 0, '9876500032', 'farhan.sheikh@iiitg.ac.in',   'Unplaced'),
('Girija Nambiar',      6, 2025, 8.25, 0, '9876500033', 'girija.nambiar@iiitg.ac.in',  'Normal'),
('Harsh Agarwal',       6, 2025, 7.10, 2, '9876500034', 'harsh.agarwal@iiitg.ac.in',   'Unplaced');

-- M.Tech ECE ME13 (dept_id = 7) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Isha Mathur',         7, 2025, 8.50, 0, '9876500035', 'isha.mathur@iiitg.ac.in',     'Normal'),
('Jayesh Kulkarni',     7, 2025, 9.15, 0, '9876500036', 'jayesh.kulkarni@iiitg.ac.in', 'Super Dream'),
('Kavya Rajan',         7, 2025, 7.85, 0, '9876500037', 'kavya.rajan@iiitg.ac.in',     'Unplaced'),
('Lokesh Pillai',       7, 2025, 8.35, 0, '9876500038', 'lokesh.pillai@iiitg.ac.in',   'Dream'),
('Mansi Trivedi',       7, 2025, 6.70, 1, '9876500039', 'mansi.trivedi@iiitg.ac.in',   'Unplaced');

-- M.Tech ECE ME14 (dept_id = 8) — 2025 batch
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Nilesh Gaikwad',      8, 2025, 8.05, 0, '9876500040', 'nilesh.gaikwad@iiitg.ac.in',  'Normal'),
('Ojaswi Prakash',      8, 2025, 9.25, 0, '9876500041', 'ojaswi.prakash@iiitg.ac.in',  'Super Dream');

-- 2024 batch (for YoY comparison)
INSERT INTO Students (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier) VALUES
('Prateek Jain',        1, 2024, 8.60, 0, '9876500042', 'prateek.jain24@iiitg.ac.in',  'Dream'),
('Rishika Das',         2, 2024, 9.20, 0, '9876500043', 'rishika.das24@iiitg.ac.in',   'Super Dream'),
('Sarthak Misra',       3, 2024, 7.90, 0, '9876500044', 'sarthak.misra24@iiitg.ac.in', 'Normal'),
('Tanya Bhatt',         5, 2024, 8.40, 0, '9876500045', 'tanya.bhatt24@iiitg.ac.in',   'Dream');

-- ============================================================
-- TABLE 5: Drives (25 rows)
-- 7 from 2024, 15 FTE from 2025, 3 upcoming internships
-- ============================================================
INSERT INTO Drives (company_id, hr_id, drive_date, role_offered, package_lpa, drive_type, status) VALUES
-- 2024 drives (completed)
(6,  6,  '2024-09-15', 'Systems Engineer',             3.60, 'FTE',       'Completed'),
(7,  7,  '2024-09-22', 'Project Engineer',             3.50, 'FTE',       'Completed'),
(9,  9,  '2024-10-05', 'Assistant System Engineer',    3.36, 'FTE',       'Completed'),
(8,  8,  '2024-10-18', 'Graduate Engineer Trainee',    3.50, 'FTE',       'Completed'),
(10, 10, '2024-11-02', 'Software Engineer',            6.00, 'FTE',       'Completed'),
(4,  4,  '2024-11-20', 'SDE Intern',                   NULL, 'Internship','Completed'),
(13, 12, '2024-12-01', 'Staff Software Engineer',     18.00, 'FTE',       'Completed'),

-- 2025 FTE drives (completed)
(1,  1,  '2025-01-10', 'Software Engineer L3',        40.00, 'FTE',       'Completed'),
(2,  2,  '2025-01-25', 'SDE II',                      35.00, 'FTE',       'Completed'),
(3,  3,  '2025-02-08', 'SDE I',                       26.00, 'FTE',       'Completed'),
(11, 11, '2025-02-15', 'Business Analyst',            28.00, 'FTE',       'Completed'),
(4,  4,  '2025-03-01', 'SDE I',                       20.00, 'FTE',       'Completed'),
(16, 14, '2025-03-12', 'Analyst',                     25.00, 'FTE',       'Completed'),
(5,  5,  '2025-03-20', 'SDE I',                       18.00, 'FTE',       'Completed'),
(14, 13, '2025-04-05', 'IC Design Engineer',          12.00, 'FTE',       'Completed'),
(18, 15, '2025-04-15', 'Backend Engineer',            16.00, 'FTE',       'Completed'),
(17, NULL,'2025-04-22', 'Technology Analyst',         20.00, 'FTE',       'Completed'),
(12, NULL,'2025-05-03', 'Business Technology Analyst',18.00, 'FTE',       'Completed'),
(6,  6,  '2025-05-10', 'Systems Engineer',             4.00, 'FTE',       'Completed'),
(7,  7,  '2025-05-18', 'Project Engineer',             3.80, 'FTE',       'Completed'),
(9,  9,  '2025-05-25', 'Assistant System Engineer',    3.60, 'FTE',       'Completed'),
(15, NULL,'2025-06-02', 'Graduate Engineer Trainee',   4.50, 'FTE',       'Completed'),

-- Upcoming internships (2025 Q3)
(1,  1,  '2025-08-15', 'Software Engineering Intern', NULL, 'Internship', 'Upcoming'),
(3,  3,  '2025-08-22', 'SDE Intern',                  NULL, 'Internship', 'Upcoming'),
(4,  4,  '2025-09-05', 'Product Intern',              NULL, 'Internship', 'Upcoming');

-- ============================================================
-- TABLE 6: DriveEligibility (90+ rows)
-- Each drive: 2–6 eligible departments with CGPA cutoffs
-- ============================================================

-- Drive 1: Infosys 2024 FTE — all 8 depts, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 1, dept_id, 6.50, 2 FROM Departments;

-- Drive 2: Wipro 2024 — all 8 depts, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 2, dept_id, 6.50, 2 FROM Departments;

-- Drive 3: TCS 2024 — all 8 depts, CGPA ≥ 6.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 3, dept_id, 6.00, 2 FROM Departments;

-- Drive 4: HCL 2024 — all 8 depts, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 4, dept_id, 6.50, 2 FROM Departments;

-- Drive 5: Persistent 2024 — CSE depts only, CGPA ≥ 7.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 5, dept_id, 7.00, 0 FROM Departments WHERE branch = 'CSE';

-- Drive 6: Flipkart Intern 2024 — CSE depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 6, dept_id, 7.50, 0 FROM Departments WHERE branch = 'CSE';

-- Drive 7: Samsung R&D 2024 — ECE + CSE M.Tech, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 7, dept_id, 7.50, 0 FROM Departments WHERE branch = 'ECE' OR (branch = 'CSE' AND program = 'M.Tech');

-- Drive 8: Google FTE 2025 — all depts, CGPA ≥ 8.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 8, dept_id, 8.00, 0 FROM Departments;

-- Drive 9: Microsoft FTE 2025 — all depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 9, dept_id, 7.50, 0 FROM Departments;

-- Drive 10: Amazon FTE 2025 — all depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 10, dept_id, 7.50, 0 FROM Departments;

-- Drive 11: McKinsey 2025 — all depts, CGPA ≥ 8.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 11, dept_id, 8.50, 0 FROM Departments;

-- Drive 12: Flipkart FTE 2025 — CSE depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 12, dept_id, 7.50, 0 FROM Departments WHERE branch = 'CSE';

-- Drive 13: Goldman Sachs 2025 — all depts, CGPA ≥ 8.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 13, dept_id, 8.00, 0 FROM Departments;

-- Drive 14: Walmart 2025 — CSE depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 14, dept_id, 7.50, 0 FROM Departments WHERE branch = 'CSE';

-- Drive 15: TI 2025 — ECE + M.Tech ECE, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 15, dept_id, 7.50, 0 FROM Departments WHERE branch = 'ECE';

-- Drive 16: Razorpay 2025 — CSE depts, CGPA ≥ 7.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 16, dept_id, 7.00, 0 FROM Departments WHERE branch = 'CSE';

-- Drive 17: Morgan Stanley 2025 — all depts, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 17, dept_id, 7.50, 0 FROM Departments;

-- Drive 18: Deloitte 2025 — all depts, CGPA ≥ 7.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 18, dept_id, 7.00, 1 FROM Departments;

-- Drive 19: Infosys 2025 FTE — all 8 depts, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 19, dept_id, 6.50, 2 FROM Departments;

-- Drive 20: Wipro 2025 FTE — all 8 depts, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 20, dept_id, 6.50, 2 FROM Departments;

-- Drive 21: TCS 2025 FTE — all 8 depts, CGPA ≥ 6.0
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 21, dept_id, 6.00, 2 FROM Departments;

-- Drive 22: Siemens 2025 — ECE depts + M.Tech CSE, CGPA ≥ 6.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 22, dept_id, 6.50, 1 FROM Departments WHERE branch = 'ECE' OR section IN ('M13','M14');

-- Drive 23: Google Intern (upcoming) — all depts, CGPA ≥ 8.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 23, dept_id, 8.50, 0 FROM Departments;

-- Drive 24: Amazon Intern (upcoming) — CSE + ECE B.Tech, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 24, dept_id, 7.50, 0 FROM Departments WHERE program = 'B.Tech';

-- Drive 25: Flipkart Intern (upcoming) — CSE B.Tech, CGPA ≥ 7.5
INSERT INTO DriveEligibility (drive_id, dept_id, min_cgpa, max_backlogs)
SELECT 25, dept_id, 7.50, 0 FROM Departments WHERE branch = 'CSE' AND program = 'B.Tech';

-- ============================================================
-- TABLE 7: Applications (70 rows) — bypass trigger
-- ============================================================

INSERT INTO Applications (student_id, drive_id, apply_date, result, offer_letter_date, waitlist_rank) VALUES
-- Google (drive 8): student_id 1 (Aarav – Super Dream selected)
(1,  8,  '2025-01-08', 'Selected',  '2025-01-15', NULL),
-- Google (drive 8): student_id 6 (Falguni – selected)
(6,  8,  '2025-01-08', 'Selected',  '2025-01-15', NULL),
-- Google (drive 8): student_id 10 (Jaya – selected)
(10, 8,  '2025-01-08', 'Selected',  '2025-01-15', NULL),
-- Google (drive 8): student_id 26 (Zara – selected)
(26, 8,  '2025-01-08', 'Selected',  '2025-01-15', NULL),
-- Google (drive 8): student_id 41 (Ojaswi – selected)
(41, 8,  '2025-01-08', 'Selected',  '2025-01-15', NULL),
-- Google (drive 8): rejected
(2,  8,  '2025-01-08', 'Rejected',  NULL, NULL),
(7,  8,  '2025-01-08', 'Rejected',  NULL, NULL),

-- Microsoft (drive 9)
(2,  9,  '2025-01-23', 'Selected',  '2025-02-01', NULL),
(7,  9,  '2025-01-23', 'Selected',  '2025-02-01', NULL),
(12, 9,  '2025-01-23', 'Selected',  '2025-02-01', NULL),
(25, 9,  '2025-01-23', 'Selected',  '2025-02-01', NULL),
(36, 9,  '2025-01-23', 'Selected',  '2025-02-01', NULL),
(4,  9,  '2025-01-23', 'Rejected',  NULL, NULL),
(9,  9,  '2025-01-23', 'Rejected',  NULL, NULL),

-- Amazon (drive 10)
(3,  10, '2025-02-05', 'Selected',  '2025-02-12', NULL),
(8,  10, '2025-02-05', 'Selected',  '2025-02-12', NULL),
(16, 10, '2025-02-05', 'Selected',  '2025-02-12', NULL),
(17, 10, '2025-02-05', 'Rejected',  NULL, NULL),
(21, 10, '2025-02-05', 'Rejected',  NULL, NULL),

-- McKinsey (drive 11)
(1,  11, '2025-02-12', 'Rejected',  NULL, NULL),   -- already SD
(6,  11, '2025-02-12', 'Rejected',  NULL, NULL),
(26, 11, '2025-02-13', 'Rejected',  NULL, NULL),

-- Flipkart FTE (drive 12)
(4,  12, '2025-02-28', 'Selected',  '2025-03-05', NULL),
(9,  12, '2025-02-28', 'Selected',  '2025-03-05', NULL),
(25, 12, '2025-02-28', 'Rejected',  NULL, NULL),
(27, 12, '2025-02-28', 'Selected',  '2025-03-05', NULL),
(30, 12, '2025-02-28', 'Rejected',  NULL, NULL),

-- Goldman Sachs (drive 13)
(1,  13, '2025-03-10', 'Selected',  '2025-03-18', NULL),
(6,  13, '2025-03-10', 'Selected',  '2025-03-18', NULL),
(10, 13, '2025-03-10', 'Rejected',  NULL, NULL),
(26, 13, '2025-03-10', 'Rejected',  NULL, NULL),

-- Walmart (drive 14)
(5,  14, '2025-03-18', 'Selected',  '2025-03-25', NULL),
(9,  14, '2025-03-18', 'Rejected',  NULL, NULL),
(29, 14, '2025-03-18', 'Selected',  '2025-03-25', NULL),
(32, 14, '2025-03-18', 'Pending',   NULL, NULL),

-- TI (drive 15) — ECE students
(13, 15, '2025-04-02', 'Selected',  '2025-04-10', NULL),
(16, 15, '2025-04-02', 'Selected',  '2025-04-10', NULL),
(35, 15, '2025-04-02', 'Rejected',  NULL, NULL),
(37, 15, '2025-04-02', 'Selected',  '2025-04-10', NULL),
(38, 15, '2025-04-02', 'Pending',   NULL, NULL),

-- Razorpay (drive 16)
(5,  16, '2025-04-13', 'Selected',  '2025-04-20', NULL),
(9,  16, '2025-04-13', 'Rejected',  NULL, NULL),
(29, 16, '2025-04-13', 'Rejected',  NULL, NULL),

-- Morgan Stanley (drive 17)
(21, 17, '2025-04-20', 'Selected',  '2025-04-28', NULL),
(23, 17, '2025-04-20', 'Selected',  '2025-04-28', NULL),
(30, 17, '2025-04-20', 'Rejected',  NULL, NULL),

-- Deloitte (drive 18)
(5,  18, '2025-05-01', 'Selected',  '2025-05-10', NULL),
(14, 18, '2025-05-01', 'Rejected',  NULL, NULL),
(20, 18, '2025-05-01', 'Pending',   NULL, NULL),
(29, 18, '2025-05-01', 'Selected',  '2025-05-10', NULL),

-- Infosys 2025 FTE (drive 19) — Normal placements
(5,  19, '2025-05-08', 'Selected',  '2025-05-15', NULL),
(11, 19, '2025-05-08', 'Selected',  '2025-05-15', NULL),
(15, 19, '2025-05-08', 'Rejected',  NULL, NULL),
(20, 19, '2025-05-08', 'Selected',  '2025-05-15', NULL),
(24, 19, '2025-05-08', 'Selected',  '2025-05-15', NULL),

-- Wipro 2025 FTE (drive 20)
(11, 20, '2025-05-16', 'Rejected',  NULL, NULL),
(15, 20, '2025-05-16', 'Selected',  '2025-05-22', NULL),
(33, 20, '2025-05-16', 'Pending',   NULL, NULL),

-- TCS 2025 FTE (drive 21)
(15, 21, '2025-05-23', 'Selected',  '2025-05-30', NULL),
(34, 21, '2025-05-23', 'Selected',  '2025-05-30', NULL),
(39, 21, '2025-05-23', 'Pending',   NULL, NULL),

-- Siemens (drive 22) — ECE
(13, 22, '2025-05-30', 'Selected',  '2025-06-05', NULL),
(35, 22, '2025-05-30', 'Rejected',  NULL, NULL),
(40, 22, '2025-05-30', 'Selected',  '2025-06-05', NULL);

-- ============================================================
-- TABLE 8: Rounds (160+ rows)
-- ============================================================

-- Helper: Insert rounds for the first ~40 applications
-- Google (app_ids 1–7): 3 rounds each
INSERT INTO Rounds (application_id, round_number, round_type, score, status) VALUES
-- App 1 (Aarav, selected)
(1, 1, 'Aptitude',  88.50, 'Pass'), (1, 2, 'Technical', 91.00, 'Pass'), (1, 3, 'HR', NULL, 'Pass'),
-- App 2 (Falguni, selected)
(2, 1, 'Aptitude',  85.00, 'Pass'), (2, 2, 'Technical', 89.50, 'Pass'), (2, 3, 'HR', NULL, 'Pass'),
-- App 3 (Jaya, selected)
(3, 1, 'Aptitude',  90.00, 'Pass'), (3, 2, 'Technical', 93.00, 'Pass'), (3, 3, 'HR', NULL, 'Pass'),
-- App 4 (Zara, selected)
(4, 1, 'Aptitude',  87.00, 'Pass'), (4, 2, 'Technical', 92.00, 'Pass'), (4, 3, 'HR', NULL, 'Pass'),
-- App 5 (Ojaswi, selected)
(5, 1, 'Aptitude',  86.00, 'Pass'), (5, 2, 'Technical', 90.00, 'Pass'), (5, 3, 'HR', NULL, 'Pass'),
-- App 6 (Bhavna, rejected)
(6, 1, 'Aptitude',  72.00, 'Pass'), (6, 2, 'Technical', 61.00, 'Fail'),
-- App 7 (Gaurav, rejected)
(7, 1, 'Aptitude',  68.00, 'Fail'),

-- Microsoft (app_ids 8–14)
(8,  1, 'Aptitude', 82.00, 'Pass'), (8,  2, 'Coding',    78.00, 'Pass'), (8,  3, 'Technical', 88.00, 'Pass'), (8,  4, 'HR', NULL, 'Pass'),
(9,  1, 'Aptitude', 80.00, 'Pass'), (9,  2, 'Coding',    76.00, 'Pass'), (9,  3, 'Technical', 85.00, 'Pass'), (9,  4, 'HR', NULL, 'Pass'),
(10, 1, 'Aptitude', 84.00, 'Pass'), (10, 2, 'Coding',    80.00, 'Pass'), (10, 3, 'Technical', 87.00, 'Pass'), (10, 4, 'HR', NULL, 'Pass'),
(11, 1, 'Aptitude', 83.00, 'Pass'), (11, 2, 'Coding',    79.00, 'Pass'), (11, 3, 'Technical', 86.00, 'Pass'), (11, 4, 'HR', NULL, 'Pass'),
(12, 1, 'Aptitude', 81.00, 'Pass'), (12, 2, 'Coding',    77.00, 'Pass'), (12, 3, 'Technical', 84.00, 'Pass'), (12, 4, 'HR', NULL, 'Pass'),
(13, 1, 'Aptitude', 70.00, 'Pass'), (13, 2, 'Coding',    62.00, 'Fail'),
(14, 1, 'Aptitude', 65.00, 'Fail'),

-- Amazon (app_ids 15–19)
(15, 1, 'Aptitude', 78.00, 'Pass'), (15, 2, 'Coding', 80.00, 'Pass'), (15, 3, 'Technical', 85.00, 'Pass'), (15, 4, 'HR', NULL, 'Pass'),
(16, 1, 'Aptitude', 76.00, 'Pass'), (16, 2, 'Coding', 74.00, 'Pass'), (16, 3, 'Technical', 82.00, 'Pass'), (16, 4, 'HR', NULL, 'Pass'),
(17, 1, 'Aptitude', 79.00, 'Pass'), (17, 2, 'Coding', 75.00, 'Pass'), (17, 3, 'Technical', 83.00, 'Pass'), (17, 4, 'HR', NULL, 'Pass'),
(18, 1, 'Aptitude', 71.00, 'Pass'), (18, 2, 'Coding', 60.00, 'Fail'),
(19, 1, 'Aptitude', 68.00, 'Pass'), (19, 2, 'Coding', 55.00, 'Fail'),

-- McKinsey (app_ids 20–22)
(20, 1, 'Aptitude', 90.00, 'Pass'), (20, 2, 'GD', NULL, 'Fail'),
(21, 1, 'Aptitude', 88.00, 'Pass'), (21, 2, 'GD', NULL, 'Fail'),
(22, 1, 'Aptitude', 91.00, 'Pass'), (22, 2, 'GD', NULL, 'Fail'),

-- Flipkart FTE (app_ids 23–27)
(23, 1, 'Aptitude', 77.00, 'Pass'), (23, 2, 'Coding', 72.00, 'Pass'), (23, 3, 'Technical', 80.00, 'Pass'), (23, 4, 'HR', NULL, 'Pass'),
(24, 1, 'Aptitude', 75.00, 'Pass'), (24, 2, 'Coding', 70.00, 'Pass'), (24, 3, 'Technical', 78.00, 'Pass'), (24, 4, 'HR', NULL, 'Pass'),
(25, 1, 'Aptitude', 71.00, 'Pass'), (25, 2, 'Coding', 63.00, 'Fail'),
(26, 1, 'Aptitude', 76.00, 'Pass'), (26, 2, 'Coding', 74.00, 'Pass'), (26, 3, 'Technical', 79.00, 'Pass'), (26, 4, 'HR', NULL, 'Pass'),
(27, 1, 'Aptitude', 66.00, 'Fail'),

-- Goldman Sachs (app_ids 28–31)
(28, 1, 'Aptitude', 85.00, 'Pass'), (28, 2, 'Technical', 88.00, 'Pass'), (28, 3, 'HR', NULL, 'Pass'),
(29, 1, 'Aptitude', 83.00, 'Pass'), (29, 2, 'Technical', 86.00, 'Pass'), (29, 3, 'HR', NULL, 'Pass'),
(30, 1, 'Aptitude', 80.00, 'Pass'), (30, 2, 'Technical', 70.00, 'Fail'),
(31, 1, 'Aptitude', 78.00, 'Pass'), (31, 2, 'Technical', 68.00, 'Fail'),

-- Walmart (app_ids 32–35)
(32, 1, 'Aptitude', 74.00, 'Pass'), (32, 2, 'Coding', 70.00, 'Pass'), (32, 3, 'Technical', 77.00, 'Pass'), (32, 4, 'HR', NULL, 'Pass'),
(33, 1, 'Aptitude', 72.00, 'Fail'),
(34, 1, 'Aptitude', 73.00, 'Pass'), (34, 2, 'Coding', 69.00, 'Pass'), (34, 3, 'Technical', 75.00, 'Pass'), (34, 4, 'HR', NULL, 'Pass'),
(35, 1, 'Aptitude', 70.00, 'Pass'), (35, 2, 'Coding', 66.00, 'Pending'),

-- TI (app_ids 36–40)
(36, 1, 'Aptitude', 76.00, 'Pass'), (36, 2, 'Technical', 80.00, 'Pass'), (36, 3, 'HR', NULL, 'Pass'),
(37, 1, 'Aptitude', 78.00, 'Pass'), (37, 2, 'Technical', 82.00, 'Pass'), (37, 3, 'HR', NULL, 'Pass'),
(38, 1, 'Aptitude', 65.00, 'Fail'),
(39, 1, 'Aptitude', 75.00, 'Pass'), (39, 2, 'Technical', 79.00, 'Pass'), (39, 3, 'HR', NULL, 'Pass'),
(40, 1, 'Aptitude', 70.00, 'Pass'), (40, 2, 'Technical', 67.00, 'Pending'),

-- Razorpay (app_ids 41–43)
(41, 1, 'Aptitude', 72.00, 'Pass'), (41, 2, 'Coding', 70.00, 'Pass'), (41, 3, 'Technical', 75.00, 'Pass'),
(42, 1, 'Aptitude', 68.00, 'Pass'), (42, 2, 'Coding', 60.00, 'Fail'),
(43, 1, 'Aptitude', 65.00, 'Fail'),

-- Morgan Stanley (app_ids 44–46)
(44, 1, 'Aptitude', 76.00, 'Pass'), (44, 2, 'Technical', 80.00, 'Pass'), (44, 3, 'HR', NULL, 'Pass'),
(45, 1, 'Aptitude', 74.00, 'Pass'), (45, 2, 'Technical', 78.00, 'Pass'), (45, 3, 'HR', NULL, 'Pass'),
(46, 1, 'Aptitude', 70.00, 'Pass'), (46, 2, 'Technical', 62.00, 'Fail'),

-- Deloitte (app_ids 47–50)
(47, 1, 'Aptitude', 72.00, 'Pass'), (47, 2, 'GD', NULL, 'Pass'), (47, 3, 'HR', NULL, 'Pass'),
(48, 1, 'Aptitude', 68.00, 'Pass'), (48, 2, 'GD', NULL, 'Fail'),
(49, 1, 'Aptitude', 70.00, 'Pass'), (49, 2, 'GD', NULL, 'Pending'),
(50, 1, 'Aptitude', 71.00, 'Pass'), (50, 2, 'GD', NULL, 'Pass'), (50, 3, 'HR', NULL, 'Pass'),

-- Infosys 2025 (app_ids 51–55)
(51, 1, 'Aptitude', 68.00, 'Pass'), (51, 2, 'Technical', 72.00, 'Pass'),
(52, 1, 'Aptitude', 66.00, 'Pass'), (52, 2, 'Technical', 70.00, 'Pass'),
(53, 1, 'Aptitude', 64.00, 'Fail'),
(54, 1, 'Aptitude', 69.00, 'Pass'), (54, 2, 'Technical', 71.00, 'Pass'),
(55, 1, 'Aptitude', 67.00, 'Pass'), (55, 2, 'Technical', 73.00, 'Pass'),

-- Wipro 2025 (app_ids 56–58)
(56, 1, 'Aptitude', 65.00, 'Fail'),
(57, 1, 'Aptitude', 67.00, 'Pass'), (57, 2, 'Technical', 69.00, 'Pass'),
(58, 1, 'Aptitude', 66.00, 'Pending'),

-- TCS 2025 (app_ids 59–61)
(59, 1, 'Aptitude', 65.00, 'Pass'), (59, 2, 'Technical', 68.00, 'Pass'),
(60, 1, 'Aptitude', 67.00, 'Pass'), (60, 2, 'Technical', 70.00, 'Pass'),
(61, 1, 'Aptitude', 64.00, 'Pending'),

-- Siemens (app_ids 62–64)
(62, 1, 'Aptitude', 68.00, 'Pass'), (62, 2, 'Technical', 72.00, 'Pass'),
(63, 1, 'Aptitude', 64.00, 'Fail'),
-- End of rounds insert
(64, 1, 'Aptitude', 69.00, 'Pass'), (64, 2, 'Technical', 73.00, 'Pass');

-- Re-enable trigger flag
SET @DISABLE_TRIGGERS = 0;

-- ── Verify row counts ────────────────────────────────────────
SELECT 'Departments'    AS tbl, COUNT(*) AS row_count FROM Departments
UNION ALL
SELECT 'Students',        COUNT(*) FROM Students
UNION ALL
SELECT 'Companies',       COUNT(*) FROM Companies
UNION ALL
SELECT 'HR_Contacts',     COUNT(*) FROM HR_Contacts
UNION ALL
SELECT 'Drives',          COUNT(*) FROM Drives
UNION ALL
SELECT 'DriveEligibility',COUNT(*) FROM DriveEligibility
UNION ALL
SELECT 'Applications',    COUNT(*) FROM Applications
UNION ALL
SELECT 'Rounds',          COUNT(*) FROM Rounds;
