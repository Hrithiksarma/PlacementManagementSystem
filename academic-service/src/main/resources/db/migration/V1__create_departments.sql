-- V1__create_departments.sql
-- Same structure as PRMS so dept_id values match on import.

CREATE TABLE departments (
    dept_id   INT          NOT NULL AUTO_INCREMENT,
    section   VARCHAR(10)  NOT NULL,
    program   VARCHAR(20)  NOT NULL,
    branch    VARCHAR(60)  NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (dept_id),
    UNIQUE KEY uq_dept_section (section)
);
