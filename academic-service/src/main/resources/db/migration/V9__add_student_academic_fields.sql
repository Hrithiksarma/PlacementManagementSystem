-- V7__add_student_academic_fields.sql
-- Adds address, class 10 %, class 12 %, and graduation CGPA
-- to the students table.
-- graduation_cgpa = B.Tech CGPA at time of M.Tech admission.
-- All columns are nullable so existing 45 seeded students
-- are not affected.

ALTER TABLE students
    ADD COLUMN address              VARCHAR(500)  NULL
        AFTER active_backlogs,
    ADD COLUMN class10_percentage   DECIMAL(5,2)  NULL
        COMMENT 'Class 10 board exam percentage (0–100)',
    ADD COLUMN class12_percentage   DECIMAL(5,2)  NULL
        COMMENT 'Class 12 board exam percentage (0–100)',
    ADD COLUMN graduation_cgpa      DECIMAL(4,2)  NULL
        COMMENT 'B.Tech CGPA at admission — required for M.Tech students, NULL for B.Tech';
