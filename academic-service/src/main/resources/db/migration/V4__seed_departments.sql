-- V4__seed_departments.sql
-- dept_id values are identical to PRMS so foreign keys
-- resolve correctly when PRMS imports by roll number.

INSERT INTO departments (dept_id, section, program, branch, dept_name) VALUES
(1, 'C13',  'B.Tech', 'CSE', 'Computer Science & Engineering'),
(2, 'C14',  'B.Tech', 'CSE', 'Computer Science & Engineering'),
(3, 'E13',  'B.Tech', 'ECE', 'Electronics & Communication Engineering'),
(4, 'E14',  'B.Tech', 'ECE', 'Electronics & Communication Engineering'),
(5, 'M13',  'M.Tech', 'CSE', 'Computer Science & Engineering'),
(6, 'M14',  'M.Tech', 'CSE', 'Computer Science & Engineering'),
(7, 'ME13', 'M.Tech', 'ECE', 'Electronics & Communication Engineering'),
(8, 'ME14', 'M.Tech', 'ECE', 'Electronics & Communication Engineering');
