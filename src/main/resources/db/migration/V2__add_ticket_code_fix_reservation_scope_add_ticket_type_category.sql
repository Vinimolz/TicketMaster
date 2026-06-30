-- ============================================================
-- V2 — Three targeted corrections to the V1 schema
--
-- 1. Reservation scope: enforce one-HELD-per-user-per-EVENT (not per ticket type).
--    Adds event_id to reservations (denormalized for index support) and replaces
--    the V1 partial unique index.
--
-- 2. Ticket enhancements: add ticket_code (UUID, proof-of-purchase identifier)
--    and scanned_at (TIMESTAMP, null = unscanned).
--
-- 3. Ticket type clarity: rename name → display_name (organizer-defined label)
--    and add category (enum-like CHECK constraint — the structural type of the tier).
-- ============================================================


-- ============================================================
-- 1. RESERVATIONS — Fix unique constraint scope to per-event
-- ============================================================

-- Drop the V1 index (scoped per ticket type — incorrect per entity-model-v1.md).
-- The intent was always one HELD reservation per user per EVENT, not per ticket type.
DROP INDEX IF EXISTS uq_one_held_reservation_per_user_ticket_type;

-- Denormalize event_id onto reservations.
-- Rationale: PostgreSQL indexes can only reference columns on the indexed table directly.
-- event_id is derivable via ticket_type_id → ticket_types.event_id, but a partial unique
-- index cannot span a JOIN. Storing event_id here is intentional denormalization:
-- it must always equal ticket_types.event_id for the same row (enforced by the FK chain
-- and application logic — no two values to keep in sync since it is set once at INSERT
-- and never changes).
ALTER TABLE reservations
    ADD COLUMN event_id BIGINT NOT NULL REFERENCES events(id);

-- Recreate the partial unique index at the correct scope: per event.
-- Only HELD reservations participate — EXPIRED, CONFIRMED, and CANCELLED rows are
-- excluded, so a user can make a new reservation for the same event after their
-- previous one is no longer active.
-- This is the DB-level fix for the check-then-act race: two concurrent INSERTs for
-- the same (user_id, event_id) with status='HELD' cannot both succeed — one gets
-- a unique constraint violation.
CREATE UNIQUE INDEX uq_one_held_reservation_per_user_event
    ON reservations (user_id, event_id)
    WHERE status = 'HELD';


-- ============================================================
-- 2. TICKETS — Add ticket_code and scanned_at
-- ============================================================

-- ticket_code: the public-facing proof-of-purchase identifier (e.g., for a QR code).
-- UUID ensures global uniqueness without exposing sequential IDs.
-- Generated at order confirmation time; never null once a ticket exists.
ALTER TABLE tickets
    ADD COLUMN ticket_code UUID        NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN scanned_at  TIMESTAMP   NULL;

-- Enforce uniqueness on ticket_code at the DB level.
ALTER TABLE tickets
    ADD CONSTRAINT uq_ticket_code UNIQUE (ticket_code);

-- ============================================================
-- 3. TICKET TYPES — Rename name → display_name, add category
-- ============================================================

-- Rename: 'name' was ambiguous. display_name is the organizer's free-text label
-- (e.g., "Gold Circle Backstage Pass", "General Standing Room").
ALTER TABLE ticket_types
    RENAME COLUMN name TO display_name;

-- category: the structural type of the ticket tier, constrained to a fixed enum.
-- This maps to a Java enum and allows type-safe business logic (e.g., VIP perks).
-- Adding a new category is a deliberate, versioned schema change — intentional,
-- not a limitation. Organizers pick a category; they define their own display_name.
ALTER TABLE ticket_types
    ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'GENERAL'
        CHECK (category IN ('GENERAL', 'VIP', 'EARLY_BIRD', 'PREMIUM'));

-- Remove the DEFAULT now that existing rows have been backfilled.
-- Future INSERTs must supply the category explicitly — no silent defaults.
ALTER TABLE ticket_types
    ALTER COLUMN category DROP DEFAULT;
