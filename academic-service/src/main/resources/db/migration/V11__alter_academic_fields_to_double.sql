-- Hibernate 6 maps Java Double to DOUBLE (float(53)), not DECIMAL.
-- Alter the new columns added in V9 so schema-validation passes.
ALTER TABLE students
    MODIFY COLUMN class10_percentage DOUBLE NULL,
    MODIFY COLUMN class12_percentage DOUBLE NULL,
    MODIFY COLUMN graduation_cgpa    DOUBLE NULL;
