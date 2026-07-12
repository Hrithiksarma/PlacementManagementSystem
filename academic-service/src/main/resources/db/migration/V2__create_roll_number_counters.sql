-- V2__create_roll_number_counters.sql
-- Exact same counter table used in PRMS.
-- One row per (dept_id, admission_year). last_serial tracks
-- the highest serial already issued in that group.

CREATE TABLE roll_number_counters (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    dept_id     INT    NOT NULL,
    admission_year INT NOT NULL,
    last_serial INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_counter_dept_year (dept_id, admission_year),
    CONSTRAINT fk_counter_dept
        FOREIGN KEY (dept_id) REFERENCES departments (dept_id)
        ON DELETE CASCADE
);
