-- Hibernate 6 maps Java Double to DOUBLE (float(53)), not DECIMAL.
-- Alter the column so schema-validation passes.
ALTER TABLE students
    MODIFY COLUMN cgpa DOUBLE NOT NULL DEFAULT 0.0;
