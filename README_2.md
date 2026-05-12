# PRMS — Placement Records Management System

**CS621 — Database Systems | IIIT Guwahati**  
Java Swing desktop application backed by MySQL for managing campus placement activities.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [User Roles](#user-roles)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [Reset / Fresh Start](#reset--fresh-start)
- [Data Summary](#data-summary)

---

## Overview

PRMS tracks campus placements end-to-end — students, companies, placement drives, eligibility criteria, multi-round interview applications, and offer results. Tier-based placement policies (Normal / Dream / Super Dream) are enforced through MySQL triggers rather than application-level code.

| Metric | Value |
|--------|-------|
| Tables | 8 (normalized to 3NF/4NF) |
| Business Triggers | 5 |
| SQL Queries | 10 (6 mandatory + 4 analytical) |
| CHECK Constraints | 18 |
| Frontend | Java Swing — 3 role-based panels |
| Backend | MySQL 8.0 via JDBC |

---

## Features

- Three separate panels for DBA, Staff, and Placement Officer — each with access limited to their role
- Placement tier eligibility enforced at the database level via triggers
- Live dashboard with KPI cards (placed count, avg package, upcoming drives, etc.)
- Full CRUD for students, companies, drives, applications, and interview rounds
- Eligibility checker — select a drive and see which students qualify and who has already applied
- Analytical reports: department-wise, company-wise, monthly trend, and tier distribution
- Optional audit log, views, and stored procedures in `08_production_features.sql`

---

## System Architecture

```
+-------------------------------------------------------------+
|                        USER LAYER                           |
|              Java Swing Desktop Application                 |
|                                                             |
|   +-------------+  +-------------+  +------------------+   |
|   |  LoginFrame |  |  AdminFrame |  |   StaffFrame     |   |
|   |  (Auth UI)  |  |   (DBA)     |  |  (Reports/KPIs)  |   |
|   +------+------+  +------+------+  +--------+---------+   |
|          |                |                  |              |
|          +----------------+------------------+              |
|                           |                                 |
|                   +-------+--------+                        |
|                   |  OfficerFrame  |                        |
|                   |  (Data Ops)    |                        |
|                   +-------+--------+                        |
+---------------------------+---------------------------------+
                            | JDBC
+---------------------------+---------------------------------+
|                    CONNECTION LAYER                         |
|              DBConnection.java  (Singleton)                 |
|         jdbc:mysql://host:port/placement_records_db         |
+---------------------------+---------------------------------+
                            |
+---------------------------+---------------------------------+
|                      MySQL DATABASE                         |
|                  placement_records_db                       |
|                                                             |
|  +------------+  +----------+  +----------+  +-----------+ |
|  |Departments |  | Students |  | Companies|  |HR_Contacts| |
|  +------------+  +----------+  +----------+  +-----------+ |
|  +------------+  +----------+  +----------+  +-----------+ |
|  |   Drives   |  |Eligibility  |Applications|  | Rounds  | |
|  |            |  |_Criteria |  |            |  |         | |
|  +------------+  +----------+  +----------+  +-----------+ |
|                                                             |
|  TRIGGERS                                                   |
|  - Tier eligibility check on application insert             |
|  - CGPA minimum validation per drive                        |
|  - Backlog restriction enforcement                          |
|  - Duplicate application prevention                         |
|  - Placement tier auto-update on selection                  |
+-------------------------------------------------------------+
```

### Application Flow

```
                    +-----------+
                    |  run.bat  |
                    +-----+-----+
                          |
                    +-----+------+
                    |   Login    |
                    | Enter MySQL|
                    | credentials|
                    +-----+------+
                          |
          +---------------+---------------+
          |               |               |
    +-----+-----+   +-----+-----+   +-----+-----+
    |   Admin   |   |   Staff   |   |  Officer  |
    |   (DBA)   |   | (Reports) |   | (CRUD Ops)|
    +-----+-----+   +-----+-----+   +-----+-----+
          |               |               |
    +-----+----+   +------+------+  +-----+----------+
    | SQL      |   | Dashboard   |  | Students       |
    | Editor   |   | Dept Report |  | Companies      |
    | Table    |   | Co. Perf.   |  | Drives         |
    | Browser  |   | Tier Dist.  |  | Applications   |
    | Schema   |   | Monthly     |  | Rounds         |
    | Info     |   | Trend       |  | Eligibility    |
    +-----+----+   +------+------+  +-----+----------+
          |               |               |
          +---------------+---------------+
                          |
                    +-----+-----+
                    | MySQL DB  |
                    | (Triggers |
                    |  fire on  |
                    |   write)  |
                    +-----------+
```

### Data Flow: Adding an Application

```
Officer selects Student + Drive
          |
          v
    INSERT INTO Applications
          |
          v
  MySQL TRIGGER fires
  +-----------------------------------+
  | Check 1: Student already placed?  |---- YES --> SIGNAL ERROR
  | Check 2: CGPA meets minimum?      |---- NO  --> SIGNAL ERROR
  | Check 3: No active backlogs?      |---- NO  --> SIGNAL ERROR
  | Check 4: Duplicate application?   |---- YES --> SIGNAL ERROR
  +----------------+------------------+
                   | All checks pass
                   v
           Application saved
           Result = 'Pending'
                   |
      Officer later updates result
                   |
            +------+------+
            |  Selected?  |
            +------+------+
        YES /             \ NO
           v               v
  TRIGGER updates     Result = Rejected
  placement_tier
  in Students table
```

---

## Database Schema

```
Departments --< Students >-- Applications --< Rounds
                    |             |
                    |          Drives >-- Eligibility_Criteria
                    |             |
                Companies <-------+
                    |
                HR_Contacts
```

| Table | Key Columns | Purpose |
|-------|-------------|---------|
| `Departments` | dept_id, dept_name, program, branch, section | 8 academic departments |
| `Students` | student_id, name, cgpa, placement_tier | Student records with tier tracking |
| `Companies` | company_id, name, sector, tier | Recruiting companies |
| `HR_Contacts` | contact_id, company_id, name, email | Company HR details |
| `Drives` | drive_id, company_id, role, package, status | Placement drive events |
| `Eligibility_Criteria` | criteria_id, drive_id, dept_id, min_cgpa | Per-drive eligibility rules |
| `Applications` | app_id, student_id, drive_id, result | Student applications to drives |
| `Rounds` | round_id, app_id, round_type, score, status | Interview round outcomes |

---

## Prerequisites

| Software | Minimum Version | Download |
|----------|----------------|----------|
| Java JDK | 11+ | https://adoptium.net/ |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| MySQL Workbench | 8.0+ | https://dev.mysql.com/downloads/workbench/ |

Verify Java is properly installed before proceeding:
```
javac -version
```
You should see `javac 11.0.x` or higher. If the command is not recognized, the JDK is either not installed or not on your PATH.

---

## Installation & Setup

### Step 0 — Get the MySQL JDBC Driver (one-time only)

The `lib/` folder needs `mysql-connector-j-8.0.33.jar`. Choose one method:

**Method A — PowerShell setup script:**
```
Right-click setup.ps1 -> "Run with PowerShell"
```

**Method B — PowerShell command:**
```powershell
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -OutFile "lib\mysql-connector-j-8.0.33.jar"
```

**Method C — Manual download:**  
Download from this URL and save the file into the `lib\` folder:  
`https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar`

After this step, `lib\` should contain `mysql-connector-j-8.0.33.jar` (around 2 MB).

---

### Step 1 — Set Up the Database

Open MySQL Workbench, connect to your local server, and run the SQL files in order. Open each file and press `Ctrl+Shift+Enter` to execute:

| Order | File | What it does |
|-------|------|-------------|
| 1 | `sql/01_schema.sql` | Creates the database and all 8 tables |
| 2 | `sql/02_triggers.sql` | Installs 5 business-rule triggers |
| 3 | `sql/03_indexes.sql` | Creates 8 performance indexes |
| 4 | `sql/04_data.sql` | Loads test data (45 students, 18 companies, 25 drives, 70 applications) |
| 5 (optional) | `sql/05_queries.sql` | Verify all 10 analytical queries work |
| 6 (optional) | `sql/06_test_triggers.sql` | Run 9 trigger validation tests |

Verify the data loaded correctly:
```sql
USE placement_records_db;
SELECT 'Students'    AS tbl, COUNT(*) AS cnt FROM Students
UNION ALL SELECT 'Companies',    COUNT(*) FROM Companies
UNION ALL SELECT 'Drives',       COUNT(*) FROM Drives
UNION ALL SELECT 'Applications', COUNT(*) FROM Applications;
-- Expected: 45 / 18 / 25 / 70
```

---

### Step 2 — Compile the Java Application

```
Double-click compile.bat
```

Expected output:
```
============================================================
 Compilation SUCCESSFUL!  Run "run.bat" to start.
============================================================
```

If compilation fails, check that the JDK is installed (not just JRE) and that the JDBC JAR is present in `lib\`.

---

## Running the Application

```
Double-click run.bat
```

The login screen will appear. Enter your MySQL connection details:

| Field | Default Value |
|-------|---------------|
| Host | `localhost` |
| Port | `3306` |
| Username | `root` (or your MySQL username) |
| Password | Your MySQL root password |
| Role | Admin / Staff / Officer |

---

## User Roles

### Database Administrator (Admin)

Full SQL access to the database. Three tabs:

| Tab | What you can do |
|-----|----------------|
| SQL Editor | Write and run any SQL statement. Press F5 or click Execute. Results appear in a scrollable table below. |
| Table Browser | Click a table name on the left to view up to 500 rows of data. |
| Schema Info | View table structure (DESCRIBE), indexes, foreign keys, and the full trigger list. |

---

### Administrative Staff (Staff)

Read-only reporting and analytics. Six tabs:

| Tab | What you see |
|-----|-------------|
| Dashboard | KPI cards — total students, placed, unplaced, companies, drives completed/upcoming, avg package, highest package |
| Dept-wise Report | Placement count, percentage, and avg CGPA per department section |
| Company Performance | Applications, selections, and rejections per company and drive |
| Tier Distribution | Breakdown of Normal / Dream / Super Dream / Unplaced students |
| Monthly Trend | Drives, applications, offers, and selection rate grouped by month |
| Top Placed Students | All selected students sorted by package, highest first |

Click "Generate Report" on any tab to load the data.

---

### Placement Officer (Officer)

Day-to-day data entry. Six tabs:

| Tab | Operations |
|-----|-----------|
| Students | View all students, search by name or email, add a new student, update CGPA / phone / tier |
| Companies | View companies, add a new company with name, sector, tier, and website |
| Drives | View drives, add a new drive, update drive status (Upcoming -> Ongoing -> Completed) |
| Applications | View applications, add a new application, update result (Pending / Selected / Rejected) |
| Rounds | Enter an Application ID to load its rounds, add a new round with type, score, and remarks |
| Eligibility | Pick a drive and see eligible students along with whether they have already applied |

---

## Project Structure

```
prms2/
├── Main.java                         # Application entry point
├── compile.bat                       # Compiles all Java source files
├── run.bat                           # Launches the application
├── setup.ps1                         # Downloads JDBC driver via PowerShell
├── test.bat                          # Runs trigger tests against the database
│
├── db/
│   └── DBConnection.java             # Singleton JDBC connection manager
│
├── ui/
│   ├── LoginFrame.java               # Login screen (credentials + role selector)
│   ├── AdminFrame.java               # DBA panel (SQL Editor, Table Browser, Schema)
│   ├── StaffFrame.java               # Staff panel (Dashboard + 5 report tabs)
│   ├── OfficerFrame.java             # Officer panel (full CRUD operations)
│   └── UIUtils.java                  # Shared Swing and JDBC helpers
│
├── sql/
│   ├── 01_schema.sql                 # Database + 8 tables with CHECK constraints
│   ├── 02_triggers.sql               # 5 business-rule triggers
│   ├── 03_indexes.sql                # 8 performance indexes
│   ├── 04_data.sql                   # Test data (students, companies, drives, etc.)
│   ├── 05_queries.sql                # 10 analytical queries
│   ├── 06_test_triggers.sql          # 9 trigger validation tests
│   └── 07_data_validation.sql        # Standalone CHECK constraint tests
│
├── lib/
│   ├── mysql-connector-j-8.0.33.jar  # MySQL JDBC driver (add via setup.ps1)
│   └── README_DOWNLOAD_DRIVER.txt    # Manual download instructions
│
├── out/                              # Compiled .class files (created by compile.bat)
│
└── docs/
    ├── Deliverable_1_2.docx
    ├── PRMS_Workflow.docx
    └── PRMS_Validation_Changes.docx
```

---

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `javac is not recognized` | JDK not installed or not in PATH | Install JDK from https://adoptium.net/ and restart Command Prompt |
| `No JAR file found in lib\` | JDBC driver missing | Run `setup.ps1` or manually place the JAR in `lib\` (see Step 0) |
| `Access denied for user 'root'` | Wrong MySQL password | Test the password directly in MySQL Workbench |
| `Communications link failure` | MySQL Server not running | Windows: Open Services, find MySQL80, click Start. Linux: `sudo systemctl start mysql` |
| `Table doesn't exist` or blank tables | SQL files not executed | Run `01_schema.sql` through `04_data.sql` in Workbench |
| `package ui does not exist` | Wrong folder layout | `Main.java`, `db/`, and `ui/` must all be at the root of the project folder |
| Trigger error on application insert | Business rule blocked the insert (expected behaviour) | Check that the student meets the drive's CGPA and department eligibility |
| CHECK constraint error on student add | Validation rejected the input | Phone must be 10 digits starting with 6-9. CGPA must be between 0.00 and 10.00. |

---

## Reset / Fresh Start

To wipe and reload the database completely:

```sql
DROP DATABASE IF EXISTS placement_records_db;
```

Then re-run `01_schema.sql` through `04_data.sql` in order.

To recompile from scratch, delete the `out\` folder and run `compile.bat` again.

---

## Data Summary

| Entity | Count |
|--------|-------|
| Departments | 8 |
| Students | 45 (41 from 2025 batch, 4 from 2024) |
| Companies | 18 (10 IT, 2 Consulting, 3 Core, 2 Finance, 1 Fintech) |
| HR Contacts | 15 |
| Drives | 25 (7 from 2024, 15 FTE from 2025, 3 upcoming internships) |
| Eligibility Rules | 90+ |
| Applications | 70 |
| Interview Rounds | 160+ |
| Business Triggers | 5 |
| CHECK Constraints | 18 |
| Performance Indexes | 8 |

---

## License

Academic project for CS621 Database Systems, IIIT Guwahati.  
© 2025 Hrithik. All rights reserved.
