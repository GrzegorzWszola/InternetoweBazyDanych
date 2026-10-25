CREATE TABLE app_user (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE category (
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE equipment (
    id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id      INT          NOT NULL REFERENCES category (id),
    name             VARCHAR(100) NOT NULL,
    inventory_number VARCHAR(40)  NOT NULL UNIQUE,
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_equipment_status
        CHECK (status IN ('AVAILABLE', 'SERVICE', 'RETIRED'))
);

CREATE TABLE reservation (
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        INT         NOT NULL REFERENCES app_user (id),
    equipment_id   INT         NOT NULL REFERENCES equipment (id),
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at      TIMESTAMP,
    returned_at    TIMESTAMP,
    condition_note TEXT,
    CONSTRAINT chk_reservation_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_reservation_status
        CHECK (status IN ('PENDING', 'APPROVED', 'ISSUED',
                          'RETURNED', 'REJECTED', 'CANCELLED'))
);

-- indexes for the availability (date overlap) query and foreign keys
CREATE INDEX idx_reservation_equipment_dates
    ON reservation (equipment_id, start_date, end_date, status);
CREATE INDEX idx_reservation_user ON reservation (user_id);
CREATE INDEX idx_equipment_category ON equipment (category_id);

-- optional sample data
INSERT INTO category (name) VALUES ('Laptops'), ('Cameras'), ('Projectors');