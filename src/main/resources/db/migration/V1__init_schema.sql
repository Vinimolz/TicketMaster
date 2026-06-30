-- ============================================================
-- V1 — Initial schema
-- All 6 entities from entity-model-v1.md
-- ============================================================

-- USERS
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','ORGANIZER','USER')),
    name          VARCHAR(255),
    phone         VARCHAR(50),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- EVENTS
CREATE TABLE events (
    id           BIGSERIAL    PRIMARY KEY,
    organizer_id BIGINT       NOT NULL REFERENCES users(id),
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    venue        VARCHAR(255),
    date         TIMESTAMP    NOT NULL,
    sales_start  TIMESTAMP    NOT NULL,
    sales_end    TIMESTAMP    NOT NULL,
    status       VARCHAR(20)  NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','CANCELLED'))
);

-- TICKET TYPES
-- This is the contested inventory resource for the overselling problem.
-- Invariant: available + held + sold = total (enforced by CHECK constraint at DB level)
CREATE TABLE ticket_types (
    id                 BIGSERIAL     PRIMARY KEY,
    event_id           BIGINT        NOT NULL REFERENCES events(id),
    name               VARCHAR(50)   NOT NULL,
    price              NUMERIC(10,2) NOT NULL,
    total_quantity     INT           NOT NULL,
    available_quantity INT           NOT NULL,
    held_quantity      INT           NOT NULL DEFAULT 0,
    sold_quantity      INT           NOT NULL DEFAULT 0,
    CONSTRAINT qty_invariant CHECK (
        available_quantity + held_quantity + sold_quantity = total_quantity
    )
);

-- RESERVATIONS
CREATE TABLE reservations (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users(id),
    ticket_type_id BIGINT      NOT NULL REFERENCES ticket_types(id),
    quantity       INT         NOT NULL CHECK (quantity >= 1 AND quantity <= 10),
    status         VARCHAR(20) NOT NULL CHECK (status IN ('HELD','EXPIRED','CONFIRMED','CANCELLED')),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at     TIMESTAMP   NOT NULL
);

-- Unique partial index: one active (HELD) reservation per user per ticket type.
-- This is the DB-level fix for the check-then-act race condition described in entity-model-v1.md.
-- Two simultaneous requests can both pass an app-level check before either commits,
-- but only ONE can satisfy this unique constraint — the other gets a constraint violation.
CREATE UNIQUE INDEX uq_one_held_reservation_per_user_ticket_type
    ON reservations (user_id, ticket_type_id)
    WHERE status = 'HELD';

-- ORDERS
CREATE TABLE orders (
    id             BIGSERIAL     PRIMARY KEY,
    user_id        BIGINT        NOT NULL REFERENCES users(id),
    reservation_id BIGINT        NOT NULL UNIQUE REFERENCES reservations(id),
    status         VARCHAR(20)   NOT NULL CHECK (status IN ('PENDING_PAYMENT','PAID','FAILED','CANCELLED')),
    total_amount   NUMERIC(10,2) NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- TICKETS
-- Proof-of-purchase artifacts. Created ONLY after a reservation is confirmed into a paid order.
-- They are NOT the contested resource — TicketType quantity counters are.
CREATE TABLE tickets (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT    NOT NULL REFERENCES orders(id),
    ticket_type_id BIGINT    NOT NULL REFERENCES ticket_types(id)
);
