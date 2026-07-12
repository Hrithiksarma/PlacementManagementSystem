-- Hibernate 6 maps Java String to VARCHAR, not CHAR.
-- Alter the column so schema-validation passes.
ALTER TABLE students
    MODIFY COLUMN roll_no VARCHAR(7) NOT NULL;
