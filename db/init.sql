CREATE TABLE department (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(150)
);

CREATE TABLE category (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE hazard_class (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    required_level      VARCHAR(20)  NOT NULL,
    safety_instructions TEXT,
    CONSTRAINT chk_hazard_required_level CHECK (required_level IN ('BASIC', 'ADVANCED', 'SPECIALIST'))
);

CREATE TABLE equipment_model (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id   BIGINT       NOT NULL REFERENCES category (id),
    department_id BIGINT       NOT NULL REFERENCES department (id),
    hazard_class_id BIGINT     REFERENCES hazard_class (id),
    name          VARCHAR(150) NOT NULL,
    description   TEXT,
    is_consumable BOOLEAN      NOT NULL DEFAULT FALSE,
    specs         JSONB
);

CREATE TABLE equipment_item (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model_id         BIGINT       NOT NULL REFERENCES equipment_model (id) ON DELETE CASCADE,
    serial_number    VARCHAR(100) NOT NULL UNIQUE,
    inventory_number VARCHAR(100) NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_equipment_status
        CHECK (status IN ('AVAILABLE', 'IN_SERVICE', 'RETIRED'))
);

CREATE TABLE app_user (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    clearance_level VARCHAR(20)  NOT NULL DEFAULT 'BASIC',
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_user_clearance_level CHECK (clearance_level IN ('BASIC', 'ADVANCED', 'SPECIALIST'))
);

CREATE TABLE reservation (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id),
    item_id     BIGINT      NOT NULL REFERENCES equipment_item (id),
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    issued_at   TIMESTAMP,
    returned_at TIMESTAMP,
    issue_note  TEXT,
    return_note TEXT,
    CONSTRAINT chk_reservation_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_reservation_status
        CHECK (status IN ('PENDING', 'APPROVED', 'ISSUED', 'RETURNED'))
);

CREATE TABLE reservation_archive (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    original_id BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id),
    item_id     BIGINT      NOT NULL REFERENCES equipment_item (id),
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    final_status VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    archived_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason      TEXT,
    CONSTRAINT chk_archive_final_status
        CHECK (final_status IN ('CANCELLED', 'REJECTED'))
);

-- indexes for performance and foreign keys
CREATE INDEX idx_reservation_item_dates
    ON reservation (item_id, start_date, end_date, status);
CREATE INDEX idx_reservation_user ON reservation (user_id);
CREATE INDEX idx_equipment_item_model ON equipment_item (model_id);
CREATE INDEX idx_equipment_model_category ON equipment_model (category_id);
CREATE INDEX idx_equipment_model_department ON equipment_model (department_id);
CREATE INDEX idx_equipment_model_hazard_class ON equipment_model (hazard_class_id);
CREATE INDEX idx_reservation_archive_user ON reservation_archive (user_id);
CREATE INDEX idx_reservation_archive_item ON reservation_archive (item_id);

-- initial dictionary data
INSERT INTO department (name, location) VALUES 
    ('Faculty of Computer Science', 'Building A, Room 101'),
    ('Faculty of Physics', 'Building B, Room 204');

INSERT INTO category (name) VALUES 
    ('Laptops'), 
    ('Cameras'), 
    ('Projectors'),
    ('Lab Instruments');

INSERT INTO hazard_class (name, required_level, safety_instructions) VALUES
    ('Laser Equipment', 'ADVANCED', 'Eye protection required. Avoid direct eye exposure.'),
    ('High Voltage', 'SPECIALIST', 'Insulated gloves required. Authorized personnel only.');

-- initial users (password: 'password123')
INSERT INTO app_user (first_name, last_name, email, password_hash, role, clearance_level) VALUES
    ('Admin', 'User', 'admin@example.com', '$2a$12$BHIhTdm5Arn/LDYhQMa9K.h.0xoPIoJfwuZnY/NFa4kFrPgaXkfEi', 'ADMIN', 'SPECIALIST'),
    ('Jan', 'Kowalski', 'user@example.com', '$2a$12$BHIhTdm5Arn/LDYhQMa9K.h.0xoPIoJfwuZnY/NFa4kFrPgaXkfEi', 'USER', 'ADVANCED');

-- initial equipment (models & items)
-- Equipment 1: Laptop
INSERT INTO equipment_model (category_id, department_id, hazard_class_id, name, description, is_consumable, specs) VALUES
    (1, 1, NULL, 'Dell XPS 15 9530', 'High-performance laptop for developers and researchers', FALSE, '{"ram": "32GB", "cpu": "Intel i7-13700H", "storage": "1TB SSD"}');

INSERT INTO equipment_item (model_id, serial_number, inventory_number, status) VALUES
    (1, 'SN-DELL-9530-001', 'INV-2024-0001', 'AVAILABLE');

-- Equipment 2: Laboratory Laser
INSERT INTO equipment_model (category_id, department_id, hazard_class_id, name, description, is_consumable, specs) VALUES
    (4, 2, 1, 'Helium-Neon Research Laser 632.8nm', 'Precision optical laboratory laser for interferometry and optics labs', FALSE, '{"wavelength": "632.8nm", "power": "5mW", "beam_diameter": "0.8mm"}');

INSERT INTO equipment_item (model_id, serial_number, inventory_number, status) VALUES
    (2, 'SN-HENE-LSR-882', 'INV-2024-0002', 'AVAILABLE');
