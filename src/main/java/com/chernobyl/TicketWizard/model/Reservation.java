package com.chernobyl.TicketWizard.model;

import java.time.LocalDateTime;

import com.chernobyl.TicketWizard.Enums.ReservationStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    // @Enumerated(EnumType.STRING) is required — without it JPA stores the ordinal
    // (integer position in the enum) instead of the name. Ordinals are fragile:
    // adding/reordering enum values silently breaks all existing rows.
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    // ── Relationships ───────────────────────────────────────────────────────────

    // Many reservations → one user (FK: reservations.user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Many reservations → one event (FK: reservations.event_id — denormalized in V2
    // to allow the partial unique index without a JOIN).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Many reservations → one ticket type (FK: reservations.ticket_type_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    // Inverse side of the OneToOne with Order.
    // The FK column (reservation_id) lives on the orders table, so Order is the FK
    // owner and carries @JoinColumn. This side uses mappedBy — no column here.
    // Returns null for HELD/EXPIRED/CANCELLED reservations that never became an Order.
    // No cascade: the Order has its own lifecycle; creating/saving a Reservation
    // should never auto-create or auto-delete an Order.
    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Order order;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Reservation() {
    }

    public Reservation(Integer quantity, ReservationStatus status, LocalDateTime createdAt,
            LocalDateTime expiresAt, User user, Event event, TicketType ticketType) {
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.user = user;
        this.event = event;
        this.ticketType = ticketType;
    }
}
