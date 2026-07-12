-- ============================================================
-- PRMS — File 12: Extra 10 Fresh Students per Department
-- Adds 10 new students per department (8 depts = 80 total).
-- All: batch_year=2025, placement_tier='Unplaced',
--      active_backlogs=0, zero Applications rows.
--
-- Phone range: 9876501041 – 9876501120
-- (11_seed_fresh_batch.sql used 9876501001 – 9876501040)
--
-- INSERT IGNORE: safe to re-run, skips duplicate phones/emails.
-- Restart the Spring Boot backend after running — DataInitializer
-- auto-creates login accounts (username=lowercasename, pwd=student123).
-- ============================================================

USE placement_records_db;

-- ── B.Tech CSE C13  (dept_id = 1) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Arnav Bose',          1, 2025, 8.55, 0, '9876501041', 'arnav.bose@iiitg.ac.in',          'Unplaced'),
('Barsha Panda',        1, 2025, 7.30, 0, '9876501042', 'barsha.panda@iiitg.ac.in',        'Unplaced'),
('Chinmay Kulkarni',    1, 2025, 6.85, 0, '9876501043', 'chinmay.kulkarni@iiitg.ac.in',    'Unplaced'),
('Drishti Malhotra',    1, 2025, 9.15, 0, '9876501044', 'drishti.malhotra@iiitg.ac.in',    'Unplaced'),
('Eshaan Kapoor',       1, 2025, 7.60, 0, '9876501045', 'eshaan.kapoor@iiitg.ac.in',       'Unplaced'),
('Falak Desai',         1, 2025, 8.20, 0, '9876501046', 'falak.desai@iiitg.ac.in',         'Unplaced'),
('Girish Pandey',       1, 2025, 7.05, 0, '9876501047', 'girish.pandey@iiitg.ac.in',       'Unplaced'),
('Himani Verma',        1, 2025, 6.50, 0, '9876501048', 'himani.verma@iiitg.ac.in',        'Unplaced'),
('Indrajit Das',        1, 2025, 8.80, 0, '9876501049', 'indrajit.das@iiitg.ac.in',        'Unplaced'),
('Jyotsna Mehra',       1, 2025, 7.45, 0, '9876501050', 'jyotsna.mehra@iiitg.ac.in',       'Unplaced');

-- ── B.Tech CSE C14  (dept_id = 2) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Kamalika Sen',        2, 2025, 8.35, 0, '9876501051', 'kamalika.sen@iiitg.ac.in',        'Unplaced'),
('Lakshay Chauhan',     2, 2025, 7.15, 0, '9876501052', 'lakshay.chauhan@iiitg.ac.in',     'Unplaced'),
('Mohini Tripathi',     2, 2025, 6.70, 0, '9876501053', 'mohini.tripathi@iiitg.ac.in',     'Unplaced'),
('Nishant Ghosh',       2, 2025, 9.00, 0, '9876501054', 'nishant.ghosh@iiitg.ac.in',       'Unplaced'),
('Ojasvi Khatri',       2, 2025, 7.75, 0, '9876501055', 'ojasvi.khatri@iiitg.ac.in',       'Unplaced'),
('Parth Joshi',         2, 2025, 8.45, 0, '9876501056', 'parth.joshi@iiitg.ac.in',         'Unplaced'),
('Qamar Siddiqui',      2, 2025, 7.20, 0, '9876501057', 'qamar.siddiqui@iiitg.ac.in',      'Unplaced'),
('Ruchika Bansal',      2, 2025, 6.60, 0, '9876501058', 'ruchika.bansal@iiitg.ac.in',      'Unplaced'),
('Siddharth Negi',      2, 2025, 8.70, 0, '9876501059', 'siddharth.negi@iiitg.ac.in',      'Unplaced'),
('Tarushi Rawat',       2, 2025, 7.40, 0, '9876501060', 'tarushi.rawat@iiitg.ac.in',       'Unplaced');

-- ── B.Tech ECE E13  (dept_id = 3) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Utkarsh Bhatt',       3, 2025, 8.10, 0, '9876501061', 'utkarsh.bhatt@iiitg.ac.in',       'Unplaced'),
('Vaishnavi Patil',     3, 2025, 7.55, 0, '9876501062', 'vaishnavi.patil@iiitg.ac.in',     'Unplaced'),
('Wasim Akram',         3, 2025, 6.80, 0, '9876501063', 'wasim.akram@iiitg.ac.in',         'Unplaced'),
('Yamini Dixit',        3, 2025, 9.25, 0, '9876501064', 'yamini.dixit@iiitg.ac.in',        'Unplaced'),
('Zoya Hussain',        3, 2025, 7.30, 0, '9876501065', 'zoya.hussain@iiitg.ac.in',        'Unplaced'),
('Abhirup Dey',         3, 2025, 8.60, 0, '9876501066', 'abhirup.dey@iiitg.ac.in',         'Unplaced'),
('Bhavesh Solanki',     3, 2025, 7.10, 0, '9876501067', 'bhavesh.solanki@iiitg.ac.in',     'Unplaced'),
('Chanchal Kumari',     3, 2025, 6.55, 0, '9876501068', 'chanchal.kumari@iiitg.ac.in',     'Unplaced'),
('Devashish Shukla',    3, 2025, 8.30, 0, '9876501069', 'devashish.shukla@iiitg.ac.in',    'Unplaced'),
('Ella Thomas',         3, 2025, 7.65, 0, '9876501070', 'ella.thomas@iiitg.ac.in',         'Unplaced');

-- ── B.Tech ECE E14  (dept_id = 4) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Firoz Ahmed',         4, 2025, 8.00, 0, '9876501071', 'firoz.ahmed@iiitg.ac.in',         'Unplaced'),
('Garima Jain',         4, 2025, 7.50, 0, '9876501072', 'garima.jain@iiitg.ac.in',         'Unplaced'),
('Harkat Singh',        4, 2025, 6.90, 0, '9876501073', 'harkat.singh@iiitg.ac.in',        'Unplaced'),
('Ipshita Roy',         4, 2025, 9.05, 0, '9876501074', 'ipshita.roy@iiitg.ac.in',         'Unplaced'),
('Jaideep Yadav',       4, 2025, 7.25, 0, '9876501075', 'jaideep.yadav@iiitg.ac.in',       'Unplaced'),
('Komal Mehta',         4, 2025, 8.50, 0, '9876501076', 'komal.mehta@iiitg.ac.in',         'Unplaced'),
('Lakshit Sharma',      4, 2025, 7.00, 0, '9876501077', 'lakshit.sharma@iiitg.ac.in',      'Unplaced'),
('Meghna Borah',        4, 2025, 6.65, 0, '9876501078', 'meghna.borah@iiitg.ac.in',        'Unplaced'),
('Neeraj Soni',         4, 2025, 8.75, 0, '9876501079', 'neeraj.soni@iiitg.ac.in',         'Unplaced'),
('Oishika Chaudhary',   4, 2025, 7.40, 0, '9876501080', 'oishika.chaudhary@iiitg.ac.in',   'Unplaced');

-- ── M.Tech CSE M13  (dept_id = 5) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Priyanshi Gupta',     5, 2025, 8.25, 0, '9876501081', 'priyanshi.gupta@iiitg.ac.in',     'Unplaced'),
('Quaiser Ali',         5, 2025, 7.70, 0, '9876501082', 'quaiser.ali@iiitg.ac.in',         'Unplaced'),
('Raghavendra Iyer',    5, 2025, 6.85, 0, '9876501083', 'raghavendra.iyer@iiitg.ac.in',    'Unplaced'),
('Saanvi Joshi',        5, 2025, 9.10, 0, '9876501084', 'saanvi.joshi@iiitg.ac.in',        'Unplaced'),
('Tanushree Ghosh',     5, 2025, 7.45, 0, '9876501085', 'tanushree.ghosh@iiitg.ac.in',     'Unplaced'),
('Ujjwal Prasad',       5, 2025, 8.60, 0, '9876501086', 'ujjwal.prasad@iiitg.ac.in',       'Unplaced'),
('Varsha Nair',         5, 2025, 7.15, 0, '9876501087', 'varsha.nair@iiitg.ac.in',         'Unplaced'),
('Wren Fernandez',      5, 2025, 6.70, 0, '9876501088', 'wren.fernandez@iiitg.ac.in',      'Unplaced'),
('Yashasvi Rao',        5, 2025, 8.40, 0, '9876501089', 'yashasvi.rao@iiitg.ac.in',        'Unplaced'),
('Zubeida Khatun',      5, 2025, 7.30, 0, '9876501090', 'zubeida.khatun@iiitg.ac.in',      'Unplaced');

-- ── M.Tech CSE M14  (dept_id = 6) ───────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Achintya Bose',       6, 2025, 8.85, 0, '9876501091', 'achintya.bose@iiitg.ac.in',       'Unplaced'),
('Bhavana Reddy',       6, 2025, 7.55, 0, '9876501092', 'bhavana.reddy@iiitg.ac.in',       'Unplaced'),
('Cyrus Daruwala',      6, 2025, 7.00, 0, '9876501093', 'cyrus.daruwala@iiitg.ac.in',      'Unplaced'),
('Dhruv Agarwal',       6, 2025, 9.30, 0, '9876501094', 'dhruv.agarwal@iiitg.ac.in',       'Unplaced'),
('Ekansh Verma',        6, 2025, 7.80, 0, '9876501095', 'ekansh.verma@iiitg.ac.in',        'Unplaced'),
('Farheen Shaikh',      6, 2025, 8.20, 0, '9876501096', 'farheen.shaikh@iiitg.ac.in',      'Unplaced'),
('Gopika Menon',        6, 2025, 6.75, 0, '9876501097', 'gopika.menon@iiitg.ac.in',        'Unplaced'),
('Himanshu Patel',      6, 2025, 7.35, 0, '9876501098', 'himanshu.patel@iiitg.ac.in',      'Unplaced'),
('Indumati Krishnan',   6, 2025, 8.65, 0, '9876501099', 'indumati.krishnan@iiitg.ac.in',   'Unplaced'),
('Jasdeep Kaur',        6, 2025, 7.10, 0, '9876501100', 'jasdeep.kaur@iiitg.ac.in',        'Unplaced');

-- ── M.Tech ECE ME13  (dept_id = 7) ──────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Kartikay Gupta',      7, 2025, 8.40, 0, '9876501101', 'kartikay.gupta@iiitg.ac.in',      'Unplaced'),
('Leelavathi Nair',     7, 2025, 7.65, 0, '9876501102', 'leelavathi.nair@iiitg.ac.in',     'Unplaced'),
('Manvendra Singh',     7, 2025, 6.90, 0, '9876501103', 'manvendra.singh@iiitg.ac.in',     'Unplaced'),
('Nalini Sharma',       7, 2025, 9.00, 0, '9876501104', 'nalini.sharma@iiitg.ac.in',       'Unplaced'),
('Onkar Joshi',         7, 2025, 7.50, 0, '9876501105', 'onkar.joshi@iiitg.ac.in',         'Unplaced'),
('Pawan Tiwari',        7, 2025, 8.15, 0, '9876501106', 'pawan.tiwari@iiitg.ac.in',        'Unplaced'),
('Riddhima Bose',       7, 2025, 7.25, 0, '9876501107', 'riddhima.bose@iiitg.ac.in',       'Unplaced'),
('Shashwat Kumar',      7, 2025, 6.60, 0, '9876501108', 'shashwat.kumar@iiitg.ac.in',      'Unplaced'),
('Trisha Banerjee',     7, 2025, 8.70, 0, '9876501109', 'trisha.banerjee@iiitg.ac.in',     'Unplaced'),
('Utpal Dey',           7, 2025, 7.40, 0, '9876501110', 'utpal.dey@iiitg.ac.in',           'Unplaced');

-- ── M.Tech ECE ME14  (dept_id = 8) ──────────────────────────
INSERT IGNORE INTO Students
    (name, dept_id, batch_year, cgpa, active_backlogs, phone, email, placement_tier)
VALUES
('Vandana Dutta',       8, 2025, 8.30, 0, '9876501111', 'vandana.dutta@iiitg.ac.in',       'Unplaced'),
('Wasif Raza',          8, 2025, 7.55, 0, '9876501112', 'wasif.raza@iiitg.ac.in',          'Unplaced'),
('Yashvi Sheth',        8, 2025, 7.00, 0, '9876501113', 'yashvi.sheth@iiitg.ac.in',        'Unplaced'),
('Zaid Mirza',          8, 2025, 9.15, 0, '9876501114', 'zaid.mirza@iiitg.ac.in',          'Unplaced'),
('Aryaman Kapoor',      8, 2025, 7.75, 0, '9876501115', 'aryaman.kapoor@iiitg.ac.in',      'Unplaced'),
('Bhargavi Rao',        8, 2025, 8.55, 0, '9876501116', 'bhargavi.rao@iiitg.ac.in',        'Unplaced'),
('Chiranjeevi Das',     8, 2025, 6.80, 0, '9876501117', 'chiranjeevi.das@iiitg.ac.in',     'Unplaced'),
('Divij Mathur',        8, 2025, 7.30, 0, '9876501118', 'divij.mathur@iiitg.ac.in',        'Unplaced'),
('Ekantika Dey',        8, 2025, 8.90, 0, '9876501119', 'ekantika.dey@iiitg.ac.in',        'Unplaced'),
('Fatima Akhtar',       8, 2025, 7.15, 0, '9876501120', 'fatima.akhtar@iiitg.ac.in',       'Unplaced');

-- ── Verify after running both 11 and 12 ─────────────────────
-- Expected: each dept shows 15 fresh_2025 rows
-- (5 from file 11 + 10 from file 12)
SELECT
    d.section,
    d.program,
    d.branch,
    COUNT(*) AS total_in_dept,
    SUM(CASE WHEN s.batch_year = 2025 AND s.placement_tier = 'Unplaced' THEN 1 ELSE 0 END) AS fresh_unplaced_2025
FROM Students s
JOIN Departments d ON s.dept_id = d.dept_id
GROUP BY d.dept_id, d.section, d.program, d.branch
ORDER BY d.program DESC, d.branch, d.section;