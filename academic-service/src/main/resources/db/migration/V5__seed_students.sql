-- V5__seed_students.sql
-- Migrates all existing PRMS students into academic_db.
-- Name is split: first word → first_name, last word → last_name,
-- middle words → middle_name (NULL for all current 2-word names).
-- Roll numbers are the same ones already assigned in PRMS.
-- Counters are seeded at the end to reflect current state.

INSERT INTO students
    (roll_no, first_name, middle_name, last_name, gender, email, phone, dept_id, admission_year, cgpa, active_backlogs)
VALUES

-- ── B.Tech CSE C13 (dept_id=1, 2025 batch) ──────────────────
('2511101', 'Aarav',       NULL, 'Sharma',     NULL, 'aarav.sharma@iiitg.ac.in',     '9876500001', 1, 2025, 8.92, 0),
('2511102', 'Bhavna',      NULL, 'Patel',      NULL, 'bhavna.patel@iiitg.ac.in',     '9876500002', 1, 2025, 7.45, 0),
('2511103', 'Chirag',      NULL, 'Mehta',      NULL, 'chirag.mehta@iiitg.ac.in',     '9876500003', 1, 2025, 6.80, 1),
('2511104', 'Disha',       NULL, 'Nair',       NULL, 'disha.nair@iiitg.ac.in',       '9876500004', 1, 2025, 9.12, 0),
('2511105', 'Eshan',       NULL, 'Verma',      NULL, 'eshan.verma@iiitg.ac.in',      '9876500005', 1, 2025, 7.90, 0),
('2511106', 'Falguni',     NULL, 'Joshi',      NULL, 'falguni.joshi@iiitg.ac.in',    '9876500006', 1, 2025, 8.34, 0),

-- ── B.Tech CSE C14 (dept_id=2, 2025 batch) ──────────────────
('2511201', 'Gaurav',      NULL, 'Singh',      NULL, 'gaurav.singh@iiitg.ac.in',     '9876500007', 2, 2025, 7.60, 0),
('2511202', 'Harini',      NULL, 'Krishnan',   NULL, 'harini.krishnan@iiitg.ac.in',  '9876500008', 2, 2025, 8.75, 0),
('2511203', 'Ishaan',      NULL, 'Gupta',      NULL, 'ishaan.gupta@iiitg.ac.in',     '9876500009', 2, 2025, 6.55, 2),
('2511204', 'Jaya',        NULL, 'Reddy',      NULL, 'jaya.reddy@iiitg.ac.in',       '9876500010', 2, 2025, 9.05, 0),
('2511205', 'Kiran',       NULL, 'Bose',       NULL, 'kiran.bose@iiitg.ac.in',       '9876500011', 2, 2025, 7.20, 0),
('2511206', 'Lavanya',     NULL, 'Iyer',       NULL, 'lavanya.iyer@iiitg.ac.in',     '9876500012', 2, 2025, 8.60, 0),

-- ── B.Tech ECE E13 (dept_id=3, 2025 batch) ──────────────────
('2512101', 'Manan',       NULL, 'Kapoor',     NULL, 'manan.kapoor@iiitg.ac.in',     '9876500013', 3, 2025, 7.80, 0),
('2512102', 'Nidhi',       NULL, 'Menon',      NULL, 'nidhi.menon@iiitg.ac.in',      '9876500014', 3, 2025, 8.45, 0),
('2512103', 'Om',          NULL, 'Prakash',    NULL, 'om.prakash@iiitg.ac.in',       '9876500015', 3, 2025, 6.90, 1),
('2512104', 'Pallavi',     NULL, 'Rao',        NULL, 'pallavi.rao@iiitg.ac.in',      '9876500016', 3, 2025, 9.30, 0),
('2512105', 'Qasim',       NULL, 'Siddiqui',   NULL, 'qasim.siddiqui@iiitg.ac.in',  '9876500017', 3, 2025, 7.15, 0),
('2512106', 'Riddhi',      NULL, 'Shah',       NULL, 'riddhi.shah@iiitg.ac.in',      '9876500018', 3, 2025, 8.20, 0),

-- ── B.Tech ECE E14 (dept_id=4, 2025 batch) ──────────────────
('2512201', 'Sahil',       NULL, 'Thakur',     NULL, 'sahil.thakur@iiitg.ac.in',     '9876500019', 4, 2025, 7.40, 0),
('2512202', 'Tanvi',       NULL, 'Desai',      NULL, 'tanvi.desai@iiitg.ac.in',      '9876500020', 4, 2025, 8.85, 0),
('2512203', 'Uday',        NULL, 'Banerjee',   NULL, 'uday.banerjee@iiitg.ac.in',    '9876500021', 4, 2025, 6.70, 0),
('2512204', 'Vasudha',     NULL, 'Chandra',    NULL, 'vasudha.chandra@iiitg.ac.in',  '9876500022', 4, 2025, 9.00, 0),
('2512205', 'Waqar',       NULL, 'Ali',        NULL, 'waqar.ali@iiitg.ac.in',        '9876500023', 4, 2025, 7.65, 0),
('2512206', 'Xena',        NULL, 'Pillai',     NULL, 'xena.pillai@iiitg.ac.in',      '9876500024', 4, 2025, 8.10, 0),

-- ── M.Tech CSE M13 (dept_id=5, 2025 batch) ──────────────────
('2521101', 'Yash',        NULL, 'Kumar',      NULL, 'yash.kumar@iiitg.ac.in',       '9876500025', 5, 2025, 8.55, 0),
('2521102', 'Zara',        NULL, 'Hussain',    NULL, 'zara.hussain@iiitg.ac.in',     '9876500026', 5, 2025, 7.30, 0),
('2521103', 'Abhinav',     NULL, 'Tiwari',     NULL, 'abhinav.tiwari@iiitg.ac.in',   '9876500027', 5, 2025, 8.90, 0),
('2521104', 'Bharat',      NULL, 'Mishra',     NULL, 'bharat.mishra@iiitg.ac.in',    '9876500028', 5, 2025, 7.10, 1),
('2521105', 'Chandni',     NULL, 'Agrawal',    NULL, 'chandni.agrawal@iiitg.ac.in',  '9876500029', 5, 2025, 9.15, 0),

-- ── M.Tech CSE M14 (dept_id=6, 2025 batch) ──────────────────
('2521201', 'Dinesh',      NULL, 'Yadav',      NULL, 'dinesh.yadav@iiitg.ac.in',     '9876500030', 6, 2025, 7.75, 0),
('2521202', 'Ekta',        NULL, 'Srivastava', NULL, 'ekta.srivastava@iiitg.ac.in',  '9876500031', 6, 2025, 8.40, 0),
('2521203', 'Farhan',      NULL, 'Sheikh',     NULL, 'farhan.sheikh@iiitg.ac.in',     '9876500032', 6, 2025, 6.95, 0),
('2521204', 'Girija',      NULL, 'Nambiar',    NULL, 'girija.nambiar@iiitg.ac.in',   '9876500033', 6, 2025, 9.20, 0),
('2521205', 'Harsh',       NULL, 'Agarwal',    NULL, 'harsh.agarwal@iiitg.ac.in',    '9876500034', 6, 2025, 7.50, 0),

-- ── M.Tech ECE ME13 (dept_id=7, 2025 batch) ─────────────────
('2522101', 'Isha',        NULL, 'Mathur',     NULL, 'isha.mathur@iiitg.ac.in',      '9876500035', 7, 2025, 8.70, 0),
('2522102', 'Jayesh',      NULL, 'Kulkarni',   NULL, 'jayesh.kulkarni@iiitg.ac.in',  '9876500036', 7, 2025, 7.25, 0),
('2522103', 'Kavya',       NULL, 'Rajan',      NULL, 'kavya.rajan@iiitg.ac.in',      '9876500037', 7, 2025, 8.00, 0),
('2522104', 'Lokesh',      NULL, 'Pillai',     NULL, 'lokesh.pillai@iiitg.ac.in',    '9876500038', 7, 2025, 7.85, 0),
('2522105', 'Mansi',       NULL, 'Trivedi',    NULL, 'mansi.trivedi@iiitg.ac.in',    '9876500039', 7, 2025, 9.05, 0),

-- ── M.Tech ECE ME14 (dept_id=8, 2025 batch) ─────────────────
('2522201', 'Nilesh',      NULL, 'Gaikwad',    NULL, 'nilesh.gaikwad@iiitg.ac.in',   '9876500040', 8, 2025, 7.60, 0),
('2522202', 'Ojaswi',      NULL, 'Prakash',    NULL, 'ojaswi.prakash@iiitg.ac.in',   '9876500041', 8, 2025, 8.30, 0),

-- ── 2024 batch students ──────────────────────────────────────
('2411101', 'Prateek',     NULL, 'Jain',       NULL, 'prateek.jain@iiitg.ac.in',     '9876500042', 1, 2024, 7.90, 0),
('2411201', 'Rishika',     NULL, 'Das',        NULL, 'rishika.das@iiitg.ac.in',       '9876500043', 2, 2024, 8.65, 0),
('2412101', 'Sarthak',     NULL, 'Misra',      NULL, 'sarthak.misra@iiitg.ac.in',     '9876500044', 3, 2024, 7.20, 1),
('2421101', 'Tanya',       NULL, 'Bhatt',      NULL, 'tanya.bhatt@iiitg.ac.in',       '9876500045', 5, 2024, 8.10, 0);

-- ── Seed counters to match current state ─────────────────────
INSERT INTO roll_number_counters (dept_id, admission_year, last_serial)
SELECT dept_id, admission_year, COUNT(*) AS last_serial
FROM students
GROUP BY dept_id, admission_year
ON DUPLICATE KEY UPDATE last_serial = VALUES(last_serial);
