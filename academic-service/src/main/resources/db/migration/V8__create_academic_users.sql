-- V8__create_academic_users.sql
-- Stores login accounts for Academic ERP administrators.
-- Seeded by DataInitializer on first startup.

CREATE TABLE academic_users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(30)  NOT NULL DEFAULT 'ACADEMIC_ADMIN',
    PRIMARY KEY (id),
    UNIQUE KEY uq_academic_username (username)
);
