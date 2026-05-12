# Placement Records Management System (PRMS)
## Comprehensive Database Constraints Report

### 1. Executive Summary
This report outlines the rigorous database-level constraints implemented across the Placement Records Management System (PRMS). By utilizing a defense-in-depth SQL strategy orchestrated across `01_schema.sql` and `07_data_validation.sql`, the database operates autonomously to repel malformed data, enforce domain rules, and protect referential integrity without relying on frontend application logic.

---

### 2. Structural & Referential Integrity (`01_schema.sql`)

At the architectural level, the PRMS strictly adheres to the 3rd and 4th Normal Forms (3NF/4NF).

#### 2.1 Database Normalization Architecture (1NF to 4NF)
The PRMS schema was meticulously engineered to eliminate data redundancy and prevent insertion, update, and deletion anomalies by strictly adhering to the fundamental normal forms:
* **First Normal Form (1NF)**: All attributes contain atomic values, and repeating groups are eliminated. For instance, rather than storing `round1_score`, `round2_score` in the `Applications` table, a separate `Rounds` table (Table 8) was created.
* **Second Normal Form (2NF)**: All tables strictly utilize single-column surrogate Primary Keys (e.g., `student_id`), inherently eliminating any partial functional dependencies. Every non-key attribute depends entirely on the whole key.
* **Third Normal Form (3NF)**: Transitive dependencies were systematically removed. The clearest example is the decoupling of `HR_Contacts` (Table 4) from `Drives` (Table 5). If HR details (name, email) were stored directly in `Drives`, they would functionally depend on `hr_id` (a non-key attribute of `Drives`), violating 3NF.
* **Fourth Normal Form (4NF)**: Multi-valued dependencies were successfully resolved through decomposition. A placement drive often permits multiple different university departments to apply, each with different CGPA requirements. Instead of redundantly duplicating the entire `Drive` row just to change the `dept_id` requirement, the mapping was isolated into the `DriveEligibility` table (Table 6).

#### 2.2 Entity Integrity (Primary Keys)
Every single table in the schema utilizes an immutable, integer-based `PRIMARY KEY` with `AUTO_INCREMENT`. This guarantees that every department, student, or application possesses a mathematically unique surrogate identifier, immune to external naming conflicts.

#### 2.3 Referential Integrity (Foreign Keys & Cascades)
To prevent orphaned data (e.g., an application pointing to a non-existent student), Foreign Keys bind the architecture together with precise `ON DELETE` directives:
* **`CASCADE`**: Used dynamically for dependent entities. For example, if a `Drive` is deleted, its matching `DriveEligibility` configurations are automatically dropped.
* **`RESTRICT`**: Used securely for transactional entities. The system explicitly blocks the deletion of a `Student` or `Company` if an `Application` currently points to them, preventing accidental historical data loss.
* **`SET NULL`**: Used gracefully for human resources. If an `HR_Contact` leaves a company and their row is deleted, the `Drives` they managed simply have their `hr_id` swapped to `NULL` rather than destroying the drive record.

#### 2.4 Domain Integrity (ENUMs)
Rather than trusting raw text fields, critical states are locked into explicit `ENUM` definitions.
* `placement_tier` is mathematically restricted to: `'Unplaced'`, `'Normal'`, `'Dream'`, `'Super Dream'`.
* `Applications.result` is strictly `'Pending'`, `'Selected'`, or `'Rejected'`.

#### 2.5 Attribute Uniqueness (`UNIQUE`)
To prevent duplicate real-world assertions, strict `UNIQUE` checks were applied:
* **Composite Uniqueness**: An application is locked by `UNIQUE KEY uq_student_drive (student_id, drive_id)`, completely preventing a student from applying to the exact same drive twice.
* **Entity Uniqueness**: By demanding `name VARCHAR(100) NOT NULL UNIQUE` in the `Companies` table and `email VARCHAR(100) UNIQUE` for `HR_Contacts`, the system mathematically prevents the creation of duplicate corporate entities or HR representatives.

---

### 3. Data Validation & Boundary Checks (`07_data_validation.sql`)

While the structure is secured, the precise logical boundaries of the data are fortified utilizing MySQL 8.0 `CHECK` constraints. These evaluate every single row on `INSERT` and `UPDATE`, immediately failing the transaction if business logic is breached.

#### 3.1 Advanced Formula & Regex Validation
To ensure perfectly sanitized text formats, Regular Expressions (`REGEXP`) operate directly inside the engine:
* **Phone Numbers**: Ensured via `CHECK (phone REGEXP '^[6-9][0-9]{9}$')`. This forces exactly 10 digits starting with an Indian mobile prefix, blocking strings like `9876543210x` or `abc`.
* **Email Precision**: Uses complex regex strings combined with `NOT LIKE '%,%'` to expressly guarantee a single, correctly formatted email address is provided, directly resolving the issue of users mistakenly inputting comma-separated lists.
* **Website Verification**: The `Companies.website` must match `^https?://.+`, requiring a valid hyperbolic protocol.
* **Blank Text Prevention**: Fields like student names are checked with `TRIM(name) <> ''`, making it physically impossible to insert a row comprising solely of empty spaces.

#### 3.2 Numerical Bounds Constraints
Business rules regarding academics and compensation are enforced numerically:
* **CGPA Bounds**: Strictly confined via `CHECK (cgpa >= 0.00 AND cgpa <= 10.00)` on both the `Students` grading tables and the `DriveEligibility` requirement tables.
* **Backlogs**: Forced to be strictly non-negative (`CHECK (active_backlogs >= 0)`).
* **Package Caps**: A drive's `package_lpa` must inherently be greater than zero, but is also capped (`<= 200.00`) to prevent typographical errors (e.g., accidentally typing 300 instead of 30.0).

#### 3.3 Temporal & Chronological Constraints
To ensure the linearity of time within the placement workflows:
* **Graduation Validity**: The `batch_year` must sit within a plausible academic window (2015–2035).
* **Causality Checks**: The most advanced temporal check is `CHECK (offer_letter_date >= apply_date)`. This guarantees a temporal paradox cannot exist where a student is officially marked as receiving an offer letter from a company *before* the date they officially applied computationally.

---

### 4. Conclusion
By strategically combining native `UNIQUE` constraints during DDL Schema generation, explicit Foreign Key cascade rules, and complex Regex `CHECK` constraints, the PRMS database acts as an autonomous validator. This architecture guarantees absolute data cleanliness independent of any frontend application flaws.
