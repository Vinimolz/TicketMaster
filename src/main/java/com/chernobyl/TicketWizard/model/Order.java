package com.chernobyl.TicketWizard.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chernobyl.TicketWizard.Enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    // ── Relationships ───────────────────────────────────────────────────────────

    // Many orders → one user (FK: orders.user_id)
    // No cascade: deleting/updating a User should never auto-affect Orders.
    // Orders are financial records that must outlive the user that placed them.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // One-to-one with Reservation — Order is the FK OWNER because orders.reservation_id
    // is the column that holds the foreign key.
    // The FK is UNIQUE in the DB (one order per reservation, ever), which maps
    // to @OneToOne rather than @ManyToOne even though it's structurally a FK.
    // No cascade: a Reservation's lifecycle is independent of the Order.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // One order → many tickets (FK: tickets.order_id)
    // Tickets are created at confirmation time and are meaningless without their Order.
    // CascadeType.ALL: persisting/removing an Order should carry its Tickets with it.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    // ── Constructors ────────────────────────────────────────────────────────────

    public Order() {
    }

    public Order(OrderStatus status, BigDecimal totalAmount, LocalDateTime createdAt,
            User user, Reservation reservation) {
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.user = user;
        this.reservation = reservation;
    }
}
