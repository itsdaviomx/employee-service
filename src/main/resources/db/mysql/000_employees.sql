-- Generated from EmployeeEntity with Hibernate (MySQL8Dialect). Change both together:
-- the CI pipeline runs every test with ddl-auto=validate against this script.
CREATE TABLE IF NOT EXISTS employees (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    active             BIT          NOT NULL,
    birth_date         DATE         NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    job_title          VARCHAR(255) NOT NULL,
    maternal_last_name VARCHAR(255),
    middle_name        VARCHAR(255),
    paternal_last_name VARCHAR(255) NOT NULL,
    sex                VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
