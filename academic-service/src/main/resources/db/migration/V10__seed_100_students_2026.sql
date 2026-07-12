-- V8__seed_100_students_2026.sql
-- 100 new 2026-batch students across all 8 departments.
-- B.Tech (depts 1-4): 13 students each = 52 total
-- M.Tech (depts 5-8): 12 students each = 48 total
-- Phone range: 9876502001 – 9876502100 (no overlap with V5)
-- M.Tech students have graduation_cgpa (their B.Tech CGPA).
-- B.Tech students have graduation_cgpa = NULL.

-- ── B.Tech CSE C13  dept_id=1  roll 2611101–2611113 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2611101','Aadhya',NULL,'Kapoor','Female','aadhya.kapoor@iiitg.ac.in','9876502001',1,2026,8.75,0,'42 MG Road, Bengaluru, Karnataka 560001',92.40,88.60,NULL),
('2611102','Bhuvan',NULL,'Reddy','Male','bhuvan.reddy@iiitg.ac.in','9876502002',1,2026,7.40,0,'15 Jubilee Hills, Hyderabad, Telangana 500033',85.20,79.80,NULL),
('2611103','Charvi',NULL,'Singh','Female','charvi.singh@iiitg.ac.in','9876502003',1,2026,9.10,0,'28 Vasant Vihar, New Delhi 110057',95.60,93.20,NULL),
('2611104','Devraj',NULL,'Pillai','Male','devraj.pillai@iiitg.ac.in','9876502004',1,2026,6.85,1,'7 Kowdiar, Thiruvananthapuram, Kerala 695003',78.40,72.00,NULL),
('2611105','Eesha',NULL,'Banerjee','Female','eesha.banerjee@iiitg.ac.in','9876502005',1,2026,8.20,0,'55 Park Street, Kolkata, West Bengal 700016',89.00,84.50,NULL),
('2611106','Farooq',NULL,'Ahmed','Male','farooq.ahmed@iiitg.ac.in','9876502006',1,2026,7.65,0,'12 Hazratganj, Lucknow, Uttar Pradesh 226001',82.60,77.40,NULL),
('2611107','Gauri',NULL,'Tiwari','Female','gauri.tiwari@iiitg.ac.in','9876502007',1,2026,8.90,0,'33 MP Nagar, Bhopal, Madhya Pradesh 462011',93.80,91.00,NULL),
('2611108','Hitesh',NULL,'Malhotra','Male','hitesh.malhotra@iiitg.ac.in','9876502008',1,2026,7.15,1,'8 Sector 17, Chandigarh 160017',80.20,75.60,NULL),
('2611109','Ishita',NULL,'Gupta','Female','ishita.gupta@iiitg.ac.in','9876502009',1,2026,9.30,0,'21 C-Scheme, Jaipur, Rajasthan 302001',97.20,95.40,NULL),
('2611110','Jatin',NULL,'Sahu','Male','jatin.sahu@iiitg.ac.in','9876502010',1,2026,6.60,2,'44 Saheed Nagar, Bhubaneswar, Odisha 751007',76.80,70.20,NULL),
('2611111','Komal',NULL,'Bhatt','Female','komal.bhatt@iiitg.ac.in','9876502011',1,2026,8.45,0,'9 Navrangpura, Ahmedabad, Gujarat 380009',91.40,87.80,NULL),
('2611112','Lalit',NULL,'Nair','Male','lalit.nair@iiitg.ac.in','9876502012',1,2026,7.30,0,'18 MG Road, Kochi, Kerala 682016',83.60,78.20,NULL),
('2611113','Mallika',NULL,'Rao','Female','mallika.rao@iiitg.ac.in','9876502013',1,2026,8.00,0,'63 Anna Salai, Chennai, Tamil Nadu 600002',88.40,83.60,NULL);

-- ── B.Tech CSE C14  dept_id=2  roll 2611201–2611213 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2611201','Nakul',NULL,'Sharma','Male','nakul.sharma@iiitg.ac.in','9876502014',2,2026,7.80,0,'4 FC Road, Pune, Maharashtra 411004',87.20,82.40,NULL),
('2611202','Oorja',NULL,'Mishra','Female','oorja.mishra@iiitg.ac.in','9876502015',2,2026,9.05,0,'30 Civil Lines, Allahabad, Uttar Pradesh 211001',94.60,92.00,NULL),
('2611203','Pranav',NULL,'Joshi','Male','pranav.joshi@iiitg.ac.in','9876502016',2,2026,6.70,1,'17 Ring Road, Surat, Gujarat 395002',77.40,71.80,NULL),
('2611204','Qurratulain',NULL,'Siddiqui','Female','qurratulain.siddiqui@iiitg.ac.in','9876502017',2,2026,8.55,0,'22 Frazer Road, Patna, Bihar 800001',90.80,86.20,NULL),
('2611205','Rohit',NULL,'Verma','Male','rohit.verma@iiitg.ac.in','9876502018',2,2026,7.20,0,'51 The Mall, Kanpur, Uttar Pradesh 208001',81.60,76.00,NULL),
('2611206','Shreya',NULL,'Das','Female','shreya.das@iiitg.ac.in','9876502019',2,2026,8.80,0,'6 GS Road, Guwahati, Assam 781005',93.20,90.60,NULL),
('2611207','Tarun',NULL,'Mehta','Male','tarun.mehta@iiitg.ac.in','9876502020',2,2026,7.55,0,'14 Andheri West, Mumbai, Maharashtra 400058',84.80,80.20,NULL),
('2611208','Uma',NULL,'Patel','Female','uma.patel@iiitg.ac.in','9876502021',2,2026,9.20,0,'38 Alkapuri, Vadodara, Gujarat 390007',96.40,94.80,NULL),
('2611209','Vaibhav',NULL,'Kulkarni','Male','vaibhav.kulkarni@iiitg.ac.in','9876502022',2,2026,6.45,2,'25 Dharampeth, Nagpur, Maharashtra 440010',75.20,69.40,NULL),
('2611210','Wamika',NULL,'Agarwal','Female','wamika.agarwal@iiitg.ac.in','9876502023',2,2026,8.35,0,'11 Sadar Bazar, Agra, Uttar Pradesh 282001',90.00,85.60,NULL),
('2611211','Ximena',NULL,'Gomes','Female','ximena.gomes@iiitg.ac.in','9876502024',2,2026,7.70,0,'3 Panaji, Goa 403001',86.40,81.80,NULL),
('2611212','Yuvraj',NULL,'Singh','Male','yuvraj.singh@iiitg.ac.in','9876502025',2,2026,8.10,0,'9 Lawrence Road, Amritsar, Punjab 143001',89.60,84.00,NULL),
('2611213','Zoya',NULL,'Ansari','Female','zoya.ansari@iiitg.ac.in','9876502026',2,2026,7.40,0,'16 AMU Campus, Aligarh, Uttar Pradesh 202002',83.20,78.60,NULL);

-- ── B.Tech ECE E13  dept_id=3  roll 2612101–2612113 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2612101','Aditi',NULL,'Sharma','Female','aditi.sharma26@iiitg.ac.in','9876502027',3,2026,8.65,0,'52 Indiranagar, Bengaluru, Karnataka 560038',92.00,88.40,NULL),
('2612102','Balram',NULL,'Yadav','Male','balram.yadav@iiitg.ac.in','9876502028',3,2026,7.25,1,'19 Rajajipuram, Lucknow, Uttar Pradesh 226017',81.00,76.60,NULL),
('2612103','Chhavi',NULL,'Dubey','Female','chhavi.dubey@iiitg.ac.in','9876502029',3,2026,9.15,0,'40 Chitrakoot, Jaipur, Rajasthan 302021',95.80,93.60,NULL),
('2612104','Deepak',NULL,'Rathore','Male','deepak.rathore@iiitg.ac.in','9876502030',3,2026,6.90,1,'7 Ratanada, Jodhpur, Rajasthan 342001',78.60,73.20,NULL),
('2612105','Ekta',NULL,'Pandey','Female','ekta.pandey@iiitg.ac.in','9876502031',3,2026,8.40,0,'23 Sigra, Varanasi, Uttar Pradesh 221010',90.40,86.80,NULL),
('2612106','Fardeen',NULL,'Khan','Male','fardeen.khan@iiitg.ac.in','9876502032',3,2026,7.60,0,'35 Malegaon, Nashik, Maharashtra 422101',84.20,79.60,NULL),
('2612107','Gopika',NULL,'Nair','Female','gopika.nair26@iiitg.ac.in','9876502033',3,2026,8.85,0,'6 Kaloor, Kochi, Kerala 682017',93.60,91.20,NULL),
('2612108','Harsh','Vardhan','Singh','Male','harsh.vardhan.singh@iiitg.ac.in','9876502034',3,2026,7.05,0,'14 Gandhi Road, Dehradun, Uttarakhand 248001',79.80,74.40,NULL),
('2612109','Ipshita',NULL,'Chakraborty','Female','ipshita.chakraborty@iiitg.ac.in','9876502035',3,2026,9.45,0,'88 Lake Town, Kolkata, West Bengal 700089',97.80,96.20,NULL),
('2612110','Jitendra',NULL,'Kumar','Male','jitendra.kumar@iiitg.ac.in','9876502036',3,2026,6.55,2,'27 Boring Road, Patna, Bihar 800001',76.00,70.80,NULL),
('2612111','Kanika',NULL,'Arora','Female','kanika.arora@iiitg.ac.in','9876502037',3,2026,8.25,0,'5 Model Town, Ludhiana, Punjab 141002',89.20,84.80,NULL),
('2612112','Lakshmesh',NULL,'Iyer','Male','lakshmesh.iyer@iiitg.ac.in','9876502038',3,2026,7.45,0,'31 T Nagar, Chennai, Tamil Nadu 600017',83.80,79.20,NULL),
('2612113','Manavi',NULL,'Srivastava','Female','manavi.srivastava@iiitg.ac.in','9876502039',3,2026,8.70,0,'12 Gomti Nagar, Lucknow, Uttar Pradesh 226010',92.60,89.00,NULL);

-- ── B.Tech ECE E14  dept_id=4  roll 2612201–2612213 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2612201','Neeraj',NULL,'Bora','Male','neeraj.bora@iiitg.ac.in','9876502040',4,2026,7.85,0,'9 Dispur, Guwahati, Assam 781006',87.60,82.80,NULL),
('2612202','Oishee',NULL,'Roy','Female','oishee.roy@iiitg.ac.in','9876502041',4,2026,9.00,0,'47 Bhowanipore, Kolkata, West Bengal 700025',94.20,91.80,NULL),
('2612203','Parnika',NULL,'Gupta','Female','parnika.gupta@iiitg.ac.in','9876502042',4,2026,6.75,1,'18 Rajpur Road, Dehradun, Uttarakhand 248001',77.80,72.40,NULL),
('2612204','Qayyum',NULL,'Sheikh','Male','qayyum.sheikh@iiitg.ac.in','9876502043',4,2026,8.50,0,'36 Begumbagh, Bhopal, Madhya Pradesh 462001',91.20,87.40,NULL),
('2612205','Rishi',NULL,'Agarwal','Male','rishi.agarwal@iiitg.ac.in','9876502044',4,2026,7.30,0,'22 Lalbagh, Lucknow, Uttar Pradesh 226001',82.40,77.00,NULL),
('2612206','Sakshi',NULL,'Misra','Female','sakshi.misra@iiitg.ac.in','9876502045',4,2026,8.95,0,'3 Sector 18, Noida, Uttar Pradesh 201301',94.80,92.40,NULL),
('2612207','Tejas',NULL,'Patel','Male','tejas.patel26@iiitg.ac.in','9876502046',4,2026,7.15,1,'57 CG Road, Ahmedabad, Gujarat 380006',80.60,75.80,NULL),
('2612208','Usha',NULL,'Menon','Female','usha.menon@iiitg.ac.in','9876502047',4,2026,9.25,0,'11 Kowdiar Road, Thiruvananthapuram, Kerala 695003',96.80,95.00,NULL),
('2612209','Vibhor',NULL,'Soni','Male','vibhor.soni@iiitg.ac.in','9876502048',4,2026,6.40,2,'29 Sindhi Colony, Jaipur, Rajasthan 302004',74.60,69.00,NULL),
('2612210','Winnie',NULL,'Fernandez','Female','winnie.fernandez@iiitg.ac.in','9876502049',4,2026,8.30,0,'8 Caranzalem, Panaji, Goa 403002',89.80,85.20,NULL),
('2612211','Xerxes',NULL,'Irani','Male','xerxes.irani@iiitg.ac.in','9876502050',4,2026,7.75,0,'14 Marine Drive, Mumbai, Maharashtra 400020',86.00,81.40,NULL),
('2612212','Yasmin',NULL,'Shaikh','Female','yasmin.shaikh@iiitg.ac.in','9876502051',4,2026,8.15,0,'26 Kalanagar, Mumbai, Maharashtra 400051',90.20,85.80,NULL),
('2612213','Zuber',NULL,'Khan','Male','zuber.khan@iiitg.ac.in','9876502052',4,2026,7.55,0,'39 Gandhinagar, Hubli, Karnataka 580032',84.40,79.80,NULL);

-- ── M.Tech CSE M13  dept_id=5  roll 2621101–2621112 ──────────
-- graduation_cgpa = B.Tech CGPA at time of M.Tech admission
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2621101','Anirudh',NULL,'Sharma','Male','anirudh.sharma@iiitg.ac.in','9876502053',5,2026,8.60,0,'5 Rajaji Nagar, Bengaluru, Karnataka 560010',88.40,85.20,8.20),
('2621102','Bhavika',NULL,'Jain','Female','bhavika.jain@iiitg.ac.in','9876502054',5,2026,7.90,0,'16 Mahal Road, Nagpur, Maharashtra 440032',84.60,80.00,7.45),
('2621103','Chetan',NULL,'Patil','Male','chetan.patil@iiitg.ac.in','9876502055',5,2026,8.25,0,'43 Katraj, Pune, Maharashtra 411046',90.20,87.60,7.80),
('2621104','Divyesh',NULL,'Modi','Male','divyesh.modi@iiitg.ac.in','9876502056',5,2026,7.55,0,'21 Ellisbridge, Ahmedabad, Gujarat 380006',82.80,78.40,7.20),
('2621105','Esha',NULL,'Krishnan','Female','esha.krishnan@iiitg.ac.in','9876502057',5,2026,9.10,0,'7 Adyar, Chennai, Tamil Nadu 600020',95.40,93.00,8.75),
('2621106','Faizan',NULL,'Mirza','Male','faizan.mirza@iiitg.ac.in','9876502058',5,2026,7.30,0,'34 Aminabad, Lucknow, Uttar Pradesh 226018',80.60,76.20,6.95),
('2621107','Geeta',NULL,'Bose','Female','geeta.bose@iiitg.ac.in','9876502059',5,2026,8.75,0,'62 Salt Lake, Kolkata, West Bengal 700064',92.80,90.40,8.40),
('2621108','Harshal',NULL,'Kulkarni','Male','harshal.kulkarni@iiitg.ac.in','9876502060',5,2026,7.65,0,'19 Kothrud, Pune, Maharashtra 411038',85.00,81.60,7.30),
('2621109','Indira',NULL,'Menon','Female','indira.menon@iiitg.ac.in','9876502061',5,2026,9.30,0,'2 Ernakulam, Kochi, Kerala 682011',97.00,95.60,9.00),
('2621110','Jaswant',NULL,'Singh','Male','jaswant.singh@iiitg.ac.in','9876502062',5,2026,7.10,0,'8 Patiala, Punjab 147001',79.40,75.00,6.80),
('2621111','Kruttika',NULL,'Desai','Female','kruttika.desai@iiitg.ac.in','9876502063',5,2026,8.45,0,'27 Borivali, Mumbai, Maharashtra 400092',91.60,89.20,8.10),
('2621112','Lavkush',NULL,'Yadav','Male','lavkush.yadav@iiitg.ac.in','9876502064',5,2026,7.80,0,'11 Jankipuram, Lucknow, Uttar Pradesh 226021',86.20,82.80,7.55);

-- ── M.Tech CSE M14  dept_id=6  roll 2621201–2621212 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2621201','Madhav',NULL,'Nair','Male','madhav.nair@iiitg.ac.in','9876502065',6,2026,8.20,0,'33 Kaloor, Kochi, Kerala 682017',89.80,86.40,7.85),
('2621202','Nandita',NULL,'Pillai','Female','nandita.pillai@iiitg.ac.in','9876502066',6,2026,7.50,0,'15 Pattom, Thiruvananthapuram, Kerala 695004',83.40,79.00,7.15),
('2621203','Omkar',NULL,'Joshi','Male','omkar.joshi26@iiitg.ac.in','9876502067',6,2026,9.05,0,'48 Aundh, Pune, Maharashtra 411007',94.60,92.20,8.65),
('2621204','Prabha',NULL,'Reddy','Female','prabha.reddy@iiitg.ac.in','9876502068',6,2026,7.75,0,'20 Himayatnagar, Hyderabad, Telangana 500029',86.80,83.40,7.40),
('2621205','Qasim',NULL,'Raza','Male','qasim.raza@iiitg.ac.in','9876502069',6,2026,8.40,0,'9 Charminar, Hyderabad, Telangana 500002',91.00,88.60,8.00),
('2621206','Ranjini',NULL,'Subramanian','Female','ranjini.subramanian@iiitg.ac.in','9876502070',6,2026,7.20,0,'41 Alwarpet, Chennai, Tamil Nadu 600018',81.20,77.80,6.90),
('2621207','Saurabh',NULL,'Agarwal','Male','saurabh.agarwal@iiitg.ac.in','9876502071',6,2026,8.85,0,'6 Gomti Nagar, Lucknow, Uttar Pradesh 226010',93.40,91.00,8.50),
('2621208','Tanmay',NULL,'Chatterjee','Male','tanmay.chatterjee@iiitg.ac.in','9876502072',6,2026,7.35,0,'17 Gariahat, Kolkata, West Bengal 700019',82.00,78.60,7.00),
('2621209','Urvashi',NULL,'Kapoor','Female','urvashi.kapoor26@iiitg.ac.in','9876502073',6,2026,9.15,0,'29 South Extension, New Delhi 110049',96.20,94.80,8.80),
('2621210','Vikas',NULL,'Pandey','Male','vikas.pandey@iiitg.ac.in','9876502074',6,2026,7.60,0,'38 Lanka, Varanasi, Uttar Pradesh 221005',84.80,81.40,7.25),
('2621211','Waheeda',NULL,'Rashid','Female','waheeda.rashid@iiitg.ac.in','9876502075',6,2026,8.30,0,'12 Nishat Bagh, Srinagar, J&K 190006',90.40,87.00,7.95),
('2621212','Xavier',NULL,'D''souza','Male','xavier.dsouza@iiitg.ac.in','9876502076',6,2026,7.90,0,'5 Bandra West, Mumbai, Maharashtra 400050',87.60,84.20,7.60);

-- ── M.Tech ECE ME13  dept_id=7  roll 2622101–2622112 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2622101','Yatindra',NULL,'Kumar','Male','yatindra.kumar@iiitg.ac.in','9876502077',7,2026,8.55,0,'23 Hazratganj, Lucknow, Uttar Pradesh 226001',92.20,89.80,8.20),
('2622102','Zoha',NULL,'Fatima','Female','zoha.fatima@iiitg.ac.in','9876502078',7,2026,7.80,0,'44 Shivaji Nagar, Pune, Maharashtra 411005',85.60,82.20,7.45),
('2622103','Aakanksha',NULL,'Tripathi','Female','aakanksha.tripathi@iiitg.ac.in','9876502079',7,2026,9.20,0,'17 Sunder Nagar, New Delhi 110003',96.00,94.60,8.85),
('2622104','Bipin',NULL,'Rout','Male','bipin.rout@iiitg.ac.in','9876502080',7,2026,7.15,1,'8 Sahid Nagar, Bhubaneswar, Odisha 751007',80.00,76.40,6.85),
('2622105','Chandrika',NULL,'Rao','Female','chandrika.rao@iiitg.ac.in','9876502081',7,2026,8.40,0,'31 Himayatnagar, Hyderabad, Telangana 500029',90.80,88.40,8.05),
('2622106','Durgesh',NULL,'Singh','Male','durgesh.singh@iiitg.ac.in','9876502082',7,2026,7.50,0,'19 BHU Campus, Varanasi, Uttar Pradesh 221005',83.60,80.20,7.15),
('2622107','Elakshi',NULL,'Bora','Female','elakshi.bora@iiitg.ac.in','9876502083',7,2026,8.80,0,'6 Silpukhuri, Guwahati, Assam 781003',94.00,92.60,8.45),
('2622108','Fouad',NULL,'Khan','Male','fouad.khan@iiitg.ac.in','9876502084',7,2026,7.25,0,'52 Abids, Hyderabad, Telangana 500001',81.40,77.00,6.90),
('2622109','Gargi',NULL,'Chatterjee','Female','gargi.chatterjee@iiitg.ac.in','9876502085',7,2026,9.35,0,'73 Jodhpur Park, Kolkata, West Bengal 700068',97.40,96.00,9.05),
('2622110','Haroon',NULL,'Rashid','Male','haroon.rashid@iiitg.ac.in','9876502086',7,2026,7.65,0,'28 Rajbagh, Srinagar, J&K 190008',85.20,81.80,7.30),
('2622111','Ikra',NULL,'Begum','Female','ikra.begum@iiitg.ac.in','9876502087',7,2026,8.25,0,'14 Fancy Bazar, Guwahati, Assam 781001',89.60,87.20,7.90),
('2622112','Jagmohan',NULL,'Verma','Male','jagmohan.verma@iiitg.ac.in','9876502088',7,2026,7.40,0,'36 Hoshangabad Road, Bhopal, MP 462026',83.00,79.60,7.05);

-- ── M.Tech ECE ME14  dept_id=8  roll 2622201–2622212 ──────────
INSERT IGNORE INTO students (roll_no,first_name,middle_name,last_name,gender,email,phone,dept_id,admission_year,cgpa,active_backlogs,address,class10_percentage,class12_percentage,graduation_cgpa) VALUES
('2622201','Kalpana',NULL,'Devi','Female','kalpana.devi@iiitg.ac.in','9876502089',8,2026,8.70,0,'11 Kamakhya, Guwahati, Assam 781010',93.00,90.60,8.35),
('2622202','Lekhraj',NULL,'Sharma','Male','lekhraj.sharma@iiitg.ac.in','9876502090',8,2026,7.30,0,'25 Sikar Road, Jaipur, Rajasthan 302023',82.40,78.00,6.95),
('2622203','Mamata',NULL,'Panda','Female','mamata.panda@iiitg.ac.in','9876502091',8,2026,9.00,0,'17 Cuttack Road, Bhubaneswar, Odisha 751006',95.20,93.80,8.65),
('2622204','Navjot',NULL,'Kaur','Female','navjot.kaur@iiitg.ac.in','9876502092',8,2026,7.70,0,'8 Ranjit Avenue, Amritsar, Punjab 143001',86.00,83.60,7.35),
('2622205','Omvir',NULL,'Singh','Male','omvir.singh@iiitg.ac.in','9876502093',8,2026,8.35,0,'43 Sector 14, Faridabad, Haryana 121007',90.60,88.20,8.00),
('2622206','Prachi',NULL,'Bajaj','Female','prachi.bajaj@iiitg.ac.in','9876502094',8,2026,7.50,0,'32 Defence Colony, New Delhi 110024',84.20,80.80,7.15),
('2622207','Qazi',NULL,'Hamid','Male','qazi.hamid@iiitg.ac.in','9876502095',8,2026,8.90,0,'19 Lal Chowk, Srinagar, J&K 190001',94.40,92.00,8.55),
('2622208','Radhika',NULL,'Pillai','Female','radhika.pillai@iiitg.ac.in','9876502096',8,2026,7.15,1,'6 Vazhuthacaud, Thiruvananthapuram, Kerala 695014',80.80,77.40,6.80),
('2622209','Sourav',NULL,'Biswas','Male','sourav.biswas@iiitg.ac.in','9876502097',8,2026,8.50,0,'54 Tollygunge, Kolkata, West Bengal 700033',91.80,89.40,8.15),
('2622210','Trupti',NULL,'Patil','Female','trupti.patil@iiitg.ac.in','9876502098',8,2026,7.85,0,'28 Kothrud, Pune, Maharashtra 411029',87.40,84.00,7.50),
('2622211','Ulhas',NULL,'Naik','Male','ulhas.naik@iiitg.ac.in','9876502099',8,2026,9.10,0,'13 Margao, Goa 403601',95.60,94.20,8.75),
('2622212','Vasanta',NULL,'Kumari','Female','vasanta.kumari@iiitg.ac.in','9876502100',8,2026,7.40,0,'37 Alwaye, Kochi, Kerala 683101',83.80,80.40,7.10);

-- ── Seed roll_number_counters for 2026 batch ─────────────────
INSERT INTO roll_number_counters (dept_id, admission_year, last_serial)
SELECT dept_id, admission_year, COUNT(*) AS last_serial
FROM   students
WHERE  admission_year = 2026
GROUP  BY dept_id, admission_year
ON DUPLICATE KEY UPDATE last_serial = VALUES(last_serial);
