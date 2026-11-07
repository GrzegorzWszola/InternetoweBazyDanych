CREATE TABLE app_user (
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    clearance_level VARCHAR(20)  NOT NULL DEFAULT 'LEVEL_1',
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_user_clearance_level CHECK (clearance_level IN ('LEVEL_1', 'LEVEL_2', 'LEVEL_3'))
);

CREATE TABLE department (
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(255)
);

CREATE TABLE category (
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE hazard_class (
    id                  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    required_level      VARCHAR(20)  NOT NULL,
    safety_instructions TEXT,
    CONSTRAINT chk_hazard_required_level CHECK (required_level IN ('LEVEL_1', 'LEVEL_2', 'LEVEL_3'))
);

CREATE TABLE equipment (
    id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id      INT          NOT NULL REFERENCES category (id),
    department_id    INT          NOT NULL REFERENCES department (id),
    hazard_class_id  INT          REFERENCES hazard_class (id),
    name             VARCHAR(100) NOT NULL,
    inventory_number VARCHAR(40)  NOT NULL UNIQUE,
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_equipment_status
        CHECK (status IN ('AVAILABLE', 'SERVICE', 'RETIRED'))
);

CREATE TABLE equipment_attribute (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    equipment_id INT          NOT NULL REFERENCES equipment (id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    value        VARCHAR(255) NOT NULL
);

CREATE TABLE reservation (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     INT         NOT NULL REFERENCES app_user (id),
    equipment_id INT        NOT NULL REFERENCES equipment (id),
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at   TIMESTAMP,
    returned_at TIMESTAMP,
    issue_note  TEXT,
    return_note TEXT,
    CONSTRAINT chk_reservation_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_reservation_status
        CHECK (status IN ('PENDING', 'APPROVED', 'ISSUED',
                          'RETURNED', 'REJECTED', 'CANCELLED'))
);

CREATE TABLE reservation_history (
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservation_id INT         NOT NULL REFERENCES reservation (id) ON DELETE CASCADE,
    changed_by     INT         NOT NULL REFERENCES app_user (id),
    old_status     VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    changed_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_history_old_status
        CHECK (old_status IS NULL OR old_status IN ('PENDING', 'APPROVED', 'ISSUED',
                                                   'RETURNED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_history_new_status
        CHECK (new_status IN ('PENDING', 'APPROVED', 'ISSUED',
                              'RETURNED', 'REJECTED', 'CANCELLED'))
);

-- indexes for performance and foreign keys
CREATE INDEX idx_reservation_equipment_dates
    ON reservation (equipment_id, start_date, end_date, status);
CREATE INDEX idx_reservation_user ON reservation (user_id);
CREATE INDEX idx_equipment_category ON equipment (category_id);
CREATE INDEX idx_equipment_department ON equipment (department_id);
CREATE INDEX idx_equipment_hazard_class ON equipment (hazard_class_id);
CREATE INDEX idx_equipment_attribute_equipment ON equipment_attribute (equipment_id);
CREATE INDEX idx_reservation_history_reservation ON reservation_history (reservation_id);
CREATE INDEX idx_reservation_history_changed_by ON reservation_history (changed_by);

-- sample initial data
INSERT INTO department (name, location) VALUES 
    ('Faculty of Computer Science', 'Building A, Room 101'),
    ('Faculty of Physics', 'Building B, Room 204');

INSERT INTO category (name) VALUES 
    ('Laptops'), 
    ('Cameras'), 
    ('Projectors'),
    ('Lab Instruments');

INSERT INTO hazard_class (name, required_level, safety_instructions) VALUES
    ('Laser Equipment', 'LEVEL_2', 'Eye protection required. Avoid direct eye exposure.'),
    ('High Voltage', 'LEVEL_3', 'Insulated gloves required. Authorized personnel only.');
