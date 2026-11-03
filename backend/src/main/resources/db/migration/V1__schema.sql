-- =====================================================================
-- KT_CDP_019 Criminal Detention Portal - V1 Schema
-- MySQL 8+. Enums stored as VARCHAR (mapped to Java enums via JPA STRING).
-- =====================================================================

-- ---------- Security / RBAC ----------
CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_name (name)
) ENGINE=InnoDB;

CREATE TABLE permissions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_name (name)
) ENGINE=InnoDB;

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(60)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perms_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_perms_perm FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB;

-- ---------- Staff ----------
CREATE TABLE staff (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT,
    staff_number VARCHAR(40)  NOT NULL,
    full_name    VARCHAR(120) NOT NULL,
    rank_title   VARCHAR(60),
    contact      VARCHAR(60),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_staff_number (staff_number),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------- Prisoners ----------
CREATE TABLE prisoners (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    prisoner_number       VARCHAR(40)  NOT NULL,
    first_name            VARCHAR(80)  NOT NULL,
    last_name             VARCHAR(80)  NOT NULL,
    date_of_birth         DATE,
    gender                VARCHAR(20),
    address               VARCHAR(255),
    emergency_contact     VARCHAR(120),
    id_type               VARCHAR(40),
    id_number             VARCHAR(80),
    admission_date        DATE         NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    facility              VARCHAR(80),
    ward                  VARCHAR(40),
    cell                  VARCHAR(40),
    expected_release_date DATE,
    photo_url             VARCHAR(255),
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            DATETIME     NOT NULL,
    updated_at            DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_prisoner_number (prisoner_number),
    KEY idx_prisoner_status (status),
    KEY idx_prisoner_last_name (last_name),
    KEY idx_prisoner_admission (admission_date),
    KEY idx_prisoner_expected_release (expected_release_date)
) ENGINE=InnoDB;

-- ---------- Cases ----------
CREATE TABLE cases (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    prisoner_id          BIGINT       NOT NULL,
    case_number          VARCHAR(60)  NOT NULL,
    fir_number           VARCHAR(60),
    crime_type           VARCHAR(80)  NOT NULL,
    crime_description    VARCHAR(1000),
    police_station       VARCHAR(120),
    court                VARCHAR(120),
    sections             VARCHAR(255),
    arrest_date          DATE,
    status               VARCHAR(30)  NOT NULL,
    investigating_officer VARCHAR(120),
    remarks              VARCHAR(1000),
    created_at           DATETIME     NOT NULL,
    updated_at           DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_case_number (case_number),
    KEY idx_case_prisoner (prisoner_id),
    KEY idx_case_crime_type (crime_type),
    KEY idx_case_court (court),
    KEY idx_case_status (status),
    CONSTRAINT fk_case_prisoner FOREIGN KEY (prisoner_id) REFERENCES prisoners (id)
) ENGINE=InnoDB;

-- ---------- Surrendered Articles ----------
CREATE TABLE surrendered_articles (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    prisoner_id   BIGINT       NOT NULL,
    name          VARCHAR(120) NOT NULL,
    category      VARCHAR(60),
    quantity      INT          NOT NULL DEFAULT 1,
    description   VARCHAR(500),
    condition_note VARCHAR(255),
    received_date DATE         NOT NULL,
    returned_date DATE,
    return_status VARCHAR(30)  NOT NULL,
    remarks       VARCHAR(500),
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_article_prisoner (prisoner_id),
    KEY idx_article_return_status (return_status),
    CONSTRAINT fk_article_prisoner FOREIGN KEY (prisoner_id) REFERENCES prisoners (id)
) ENGINE=InnoDB;

-- ---------- Release Records ----------
CREATE TABLE release_records (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    prisoner_id           BIGINT       NOT NULL,
    expected_release_date DATE         NOT NULL,
    release_type          VARCHAR(40)  NOT NULL,
    reason                VARCHAR(500),
    status                VARCHAR(30)  NOT NULL,
    remarks               VARCHAR(500),
    authorized_by         BIGINT,
    actual_release_date   DATE,
    created_at            DATETIME     NOT NULL,
    updated_at            DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_release_expected_date (expected_release_date),
    KEY idx_release_status (status),
    KEY idx_release_prisoner (prisoner_id),
    CONSTRAINT fk_release_prisoner FOREIGN KEY (prisoner_id) REFERENCES prisoners (id),
    CONSTRAINT fk_release_authorized_by FOREIGN KEY (authorized_by) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------- Duty Register ----------
CREATE TABLE duty_register (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    staff_id   BIGINT       NOT NULL,
    location   VARCHAR(120) NOT NULL,
    purpose    VARCHAR(255) NOT NULL,
    duty_date  DATE         NOT NULL,
    start_time TIME         NOT NULL,
    end_time   TIME         NOT NULL,
    remarks    VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_duty_staff (staff_id),
    KEY idx_duty_date (duty_date),
    KEY idx_duty_location (location),
    CONSTRAINT fk_duty_staff FOREIGN KEY (staff_id) REFERENCES staff (id),
    CONSTRAINT fk_duty_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------- Interview Requests ----------
CREATE TABLE interview_requests (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    prisoner_id    BIGINT       NOT NULL,
    visitor_name   VARCHAR(120) NOT NULL,
    relationship   VARCHAR(60),
    contact        VARCHAR(80),
    id_details     VARCHAR(120),
    requested_date DATE         NOT NULL,
    requested_time TIME,
    purpose        VARCHAR(500),
    status         VARCHAR(30)  NOT NULL,
    reviewed_by    BIGINT,
    review_date    DATETIME,
    remarks        VARCHAR(500),
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_interview_prisoner (prisoner_id),
    KEY idx_interview_status (status),
    KEY idx_interview_date (requested_date),
    CONSTRAINT fk_interview_prisoner FOREIGN KEY (prisoner_id) REFERENCES prisoners (id),
    CONSTRAINT fk_interview_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------- Movements (In-Out Register) ----------
CREATE TABLE movements (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    person_type     VARCHAR(20)  NOT NULL,
    person_ref_id   BIGINT,
    person_name     VARCHAR(120) NOT NULL,
    movement_type   VARCHAR(30)  NOT NULL,
    destination     VARCHAR(255),
    reason          VARCHAR(500),
    movement_date   DATE         NOT NULL,
    time_out        DATETIME,
    expected_return DATETIME,
    actual_return   DATETIME,
    escort_officer  BIGINT,
    status          VARCHAR(30)  NOT NULL,
    remarks         VARCHAR(500),
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_movement_status (status),
    KEY idx_movement_type (movement_type),
    KEY idx_movement_date (movement_date),
    KEY idx_movement_person (person_ref_id),
    CONSTRAINT fk_movement_escort FOREIGN KEY (escort_officer) REFERENCES staff (id)
) ENGINE=InnoDB;

-- ---------- Court Hearings (court movement specialization) ----------
CREATE TABLE court_hearings (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    movement_id     BIGINT,
    prisoner_id     BIGINT       NOT NULL,
    case_id         BIGINT,
    court           VARCHAR(120) NOT NULL,
    hearing_date    DATE         NOT NULL,
    destination     VARCHAR(255),
    departure_time  DATETIME,
    expected_return DATETIME,
    actual_return   DATETIME,
    escort_officer  BIGINT,
    status          VARCHAR(30)  NOT NULL,
    outcome         VARCHAR(500),
    remarks         VARCHAR(500),
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_court_movement (movement_id),
    KEY idx_court_hearing_date (hearing_date),
    KEY idx_court_status (status),
    KEY idx_court_prisoner (prisoner_id),
    CONSTRAINT fk_court_movement FOREIGN KEY (movement_id) REFERENCES movements (id),
    CONSTRAINT fk_court_prisoner FOREIGN KEY (prisoner_id) REFERENCES prisoners (id),
    CONSTRAINT fk_court_case FOREIGN KEY (case_id) REFERENCES cases (id),
    CONSTRAINT fk_court_escort FOREIGN KEY (escort_officer) REFERENCES staff (id)
) ENGINE=InnoDB;

-- ---------- Notifications ----------
CREATE TABLE notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_user_id BIGINT,
    type              VARCHAR(40)  NOT NULL,
    title             VARCHAR(160) NOT NULL,
    message           VARCHAR(500) NOT NULL,
    entity_type       VARCHAR(40),
    entity_id         BIGINT,
    read_flag         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notif_recipient (recipient_user_id),
    KEY idx_notif_read (read_flag),
    KEY idx_notif_created (created_at),
    CONSTRAINT fk_notif_user FOREIGN KEY (recipient_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------- Audit Logs (append-only) ----------
CREATE TABLE audit_logs (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT,
    username_snapshot VARCHAR(60),
    action           VARCHAR(60)  NOT NULL,
    entity_type      VARCHAR(60)  NOT NULL,
    entity_id        VARCHAR(60),
    old_value        TEXT,
    new_value        TEXT,
    ip_address       VARCHAR(60),
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_user (user_id),
    KEY idx_audit_entity (entity_type),
    KEY idx_audit_created (created_at)
) ENGINE=InnoDB;
