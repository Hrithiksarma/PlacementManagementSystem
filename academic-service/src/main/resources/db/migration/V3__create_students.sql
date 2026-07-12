-- V3__create_students.sql

CREATE TABLE students (
    student_id      BIGINT          NOT NULL AUTO_INCREMENT,
    roll_no         CHAR(7)         NOT NULL,
    first_name      VARCHAR(60)     NOT NULL,
    middle_name     VARCHAR(60)     NULL,
    last_name       VARCHAR(60)     NOT NULL,
    gender          VARCHAR(10)     NULL,
    email           VARCHAR(120)    NOT NULL,
    phone           VARCHAR(15)     NOT NULL,
    dept_id         INT             NOT NULL,
    admission_year  INT             NOT NULL,
    cgpa            DECIMAL(4,2)    NOT NULL DEFAULT 0.00,
    active_backlogs INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (student_id),
    UNIQUE KEY uq_student_roll  (roll_no),
    UNIQUE KEY uq_student_email (email),
    UNIQUE KEY uq_student_phone (phone),
    CONSTRAINT fk_student_dept
        FOREIGN KEY (dept_id) REFERENCES departments (dept_id),
    CONSTRAINT chk_cgpa
        CHECK (cgpa BETWEEN 0.00 AND 10.00),
    CONSTRAINT chk_backlogs
        CHECK (active_backlogs >= 0),
    CONSTRAINT chk_gender
        CHECK (gender IN ('Male', 'Female', 'Other') OR gender IS NULL)
);
