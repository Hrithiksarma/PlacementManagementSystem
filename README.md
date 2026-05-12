# Placement Records Management System (PRMS)


---

## Project Overview

A relational database system for managing campus placement activities at IIIT Guwahati. The system tracks students, companies, placement drives, HR contacts, eligibility criteria, multi-round applications, and enforces tier-based placement policies (Normal / Dream / Super Dream) through database triggers.

**Schema:** 4 main entities normalized into 8 tables  
**Triggers:** 5 business rule enforcement triggers  
**Queries:** 6 mandatory + 4 analytical = 10 total  
**Validation:** 18 CHECK constraints across all tables  
**Production Features:** Audit log, views, stored procedures, user roles  
**Frontend:** Java Swing GUI — 3 role-based panels (DBA / Staff / Officer)  

---

## Project File Structure

```
prms/
├── src/
│   ├── Main.java                      # Application entry point
│   ├── db/
│   │   └── DBConnection.java          # Singleton JDBC connection manager
│   └── ui/
│       ├── LoginFrame.java            # Login screen (MySQL credentials + role)
│       ├── AdminFrame.java            # DBA panel: SQL Editor, Table Browser, Schema Info
│       ├── StaffFrame.java            # Staff panel: Dashboard, reports, analytics
│       ├── OfficerFrame.java          # Officer panel: CRUD for all entities
│       └── UIUtils.java               # Shared Swing + JDBC helpers
├── sql/
│   ├── 01_schema.sql                  # Database + 8 tables (with CHECK constraints)
│   ├── 02_triggers.sql                # 5 business rule triggers
│   ├── 03_indexes.sql                 # 8 performance indexes
│   ├── 04_data.sql                    # Test data (45 students, 18 companies, etc.)
│   ├── 05_queries.sql                 # All 10 queries (6 mandatory + 4 analytical)
│   ├── 06_test_triggers.sql           # 9 verification tests
│   ├── 07_data_validation.sql         # Standalone CHECK constraints
│   └── 08_production_features.sql     # Audit log, views, SPs, user roles
├── lib/
│   ├── README_DOWNLOAD_DRIVER.txt     # Driver download instructions
│   └── mysql-connector-j-8.0.33.jar  # ← YOU MUST ADD THIS (see Step 0 below)
├── out/                               # Compiled .class files (auto-created)
├── docs/
│   ├── Deliverable_1_2.docx
│   ├── PRMS_Workflow.docx
│   └── PRMS_Validation_Changes.docx
├── setup.ps1                          # Auto-downloads JDBC driver (Windows PowerShell)
├── compile.bat                        # Compiles all Java source files
├── run.bat                            # Launches the application
└── README.md                          # This file
```

---

## Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| Java JDK    | 11+     | https://adoptium.net/ |
| MySQL Server| 8.0+    | https://dev.mysql.com/downloads/mysql/ |
| MySQL Workbench | 8.0+ | https://dev.mysql.com/downloads/workbench/ |

---

## How to Run — Step by Step

### Step 0 — Get the MySQL JDBC Driver (one time only)

The `lib/` folder needs the MySQL Connector/J JAR file. Choose one method:

**Method A — Run the setup script (easiest):**
```
Right-click setup.ps1 → "Run with PowerShell"
```

**Method B — PowerShell command:**
```powershell
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -OutFile "lib\mysql-connector-j-8.0.33.jar"
```

**Method C — Browser download:**  
Go to: https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar  
Save the file to the `lib\` folder inside your `prms\` folder.

After this, your `lib\` folder should contain `mysql-connector-j-8.0.33.jar` (~2 MB).

---

### Step 1 — Set Up the Database in MySQL Workbench

Run the SQL files **in order**. Open each in Workbench and press `Ctrl+Shift+Enter`:

| File | What it does |
|------|-------------|
| `sql/01_schema.sql` | Creates database + all 8 tables with CHECK constraints |
| `sql/02_triggers.sql` | Creates 5 business logic triggers |
| `sql/03_indexes.sql` | Creates 8 performance indexes |
| `sql/04_data.sql` | Inserts all test data (45 students, 18 companies, etc.) |
| `sql/05_queries.sql` | Run this to verify all 10 queries work |
| `sql/06_test_triggers.sql` | Optional: tests triggers and validation |
| `sql/08_production_features.sql` | Optional: audit log, views, SPs, user roles |

**Verify the database loaded correctly:**
```sql
USE placement_records_db;
SELECT 'Students' AS tbl, COUNT(*) FROM Students
UNION ALL SELECT 'Companies', COUNT(*) FROM Companies
UNION ALL SELECT 'Drives', COUNT(*) FROM Drives
UNION ALL SELECT 'Applications', COUNT(*) FROM Applications;
-- Expected: 45 / 18 / 25 / 70
```

---

### Step 2 — Compile the Java Application

```
Double-click compile.bat
```

You should see:
```
Compilation SUCCESSFUL!
Run "run.bat" to start.
```

If you see errors, check that:
- Java JDK (not just JRE) is installed: `javac -version` in Command Prompt
- The JDBC JAR is in `lib\` (Step 0)

---

### Step 3 — Run the Application

```
Double-click run.bat
```

The login screen appears. Enter your MySQL credentials and select a role.

---

## Login — MySQL Credentials

| Field | Default Value |
|-------|---------------|
| Host | `localhost` |
| Port | `3306` |
| Username | `root` (or your MySQL username) |
| Password | your MySQL root password |
| Role | Choose one of the three roles below |

---

## User Roles & What Each Panel Does

### 🔵 Database Administrator (`AdminFrame`)

Designed for the DBA. Three tabs:

| Tab | What you can do |
|-----|----------------|
| **SQL Editor** | Type and execute any SQL (SELECT, INSERT, UPDATE, DELETE). Press F5 or click Execute. Results display in a table below. |
| **Table Browser** | Click a table name in the left panel to view its data (up to 500 rows). |
| **Schema Info** | Select a table and click DESCRIBE, SHOW INDEXES, or Foreign Keys to inspect the schema. Trigger list shown at the bottom. |

---

### 🟢 Administrative Staff (`StaffFrame`)

Designed for placement cell reporting. Six tabs:

| Tab | What you see |
|-----|-------------|
| **Dashboard** | 8 live KPI cards — total students, placed, unplaced, companies, drives completed/upcoming, avg package, highest package. Click Refresh to update. |
| **Dept-wise Report** | Placement count, %, avg CGPA per department section. |
| **Company Performance** | Applications, selections, rejections per drive and company. |
| **Tier Distribution** | Count of Normal / Dream / Super Dream / Unplaced students with CGPA stats. |
| **Monthly Trend** | Drives, applications, offers, selection rate per month. |
| **Top Placed Students** | All selected students sorted by package descending. |

Click **▶ Generate Report** on any tab to load data. Use **Copy SQL** to copy the query for Workbench.

---

### 🟡 Placement Officer (`OfficerFrame`)

Designed for day-to-day data operations. Six tabs:

| Tab | Operations |
|-----|-----------|
| **Students** | View all students. Search by name or email. Add new student (form dialog). Update CGPA / phone / placement tier for selected row. |
| **Companies** | View all companies. Add new company with name, sector, tier, website. |
| **Drives** | View all drives. Add a new drive (select company, enter date/role/package). Update drive status (Upcoming → Ongoing → Completed). |
| **Applications** | View all applications. Add a new application (pick student + drive). Update result (Pending/Selected/Rejected) with optional offer date. |
| **Rounds** | Enter an Application ID and click Load Rounds. Add a new round (type, score, status, remarks). |
| **Eligibility** | Load all drives into the dropdown, select one, click Check — shows eligibility criteria per department AND a list of all eligible students with whether they've already applied. |

---

## Troubleshooting

### "javac is not recognized"
Java JDK is not installed or not in PATH.  
Fix: Install JDK from https://adoptium.net/ and restart your terminal.

### "No JAR file found in lib\"
The JDBC driver is missing.  
Fix: Run `setup.ps1` or manually download the JAR (see Step 0).

### "Connection failed: Access denied for user 'root'"
Wrong MySQL password in the login screen.  
Fix: Use MySQL Workbench to verify your root password works.

### "Connection failed: Communications link failure"
MySQL Server is not running.  
Fix (Windows): Open Services → find MySQL80 → click Start.  
Fix (Linux): `sudo systemctl start mysql`

### "Table doesn't exist" or blank tables in the app
The SQL files haven't been run yet, or were run against the wrong database.  
Fix: Run 01_schema.sql through 04_data.sql in MySQL Workbench.

### Compilation error: "package ui does not exist"
The source files are not in the correct folder structure.  
Fix: Verify your folder looks like this:
```
prms/src/Main.java
prms/src/db/DBConnection.java
prms/src/ui/LoginFrame.java
prms/src/ui/AdminFrame.java
prms/src/ui/StaffFrame.java
prms/src/ui/OfficerFrame.java
prms/src/ui/UIUtils.java
```

### Trigger error when adding application
The PRMS business logic triggers are working correctly.  
For example: "INELIGIBLE: Student CGPA does not meet minimum" means the database is correctly rejecting invalid data. Use a student who meets the drive's eligibility criteria.

### CHECK constraint error when adding student
This is the validation working as intended. Common causes:
- Phone must be 10 digits starting with 6–9
- Email cannot contain commas or spaces
- CGPA must be between 0.00 and 10.00

---

## Reset Everything (Start Fresh)

To wipe and reload the database:
```sql
DROP DATABASE IF EXISTS placement_records_db;
```
Then re-run sql files 01 through 04 in order.

To recompile the Java app:
```
Delete the out\ folder, then run compile.bat again.
```

---

## Data Summary

| Entity | Count |
|--------|-------|
| Departments | 8 |
| Students | 45 (41 from 2025 batch + 4 from 2024) |
| Companies | 18 (10 IT, 2 Consulting, 3 Core, 2 Finance, 1 Fintech) |
| HR Contacts | 15 |
| Drives | 25 (7 from 2024, 15 FTE from 2025, 3 upcoming internships) |
| Eligibility Rules | 90+ |
| Applications | 70 |
| Rounds | 160+ |
| Business Triggers | 6 |
| CHECK Constraints | 18 |
| Performance Indexes | 8 |

---


