package com.chernobyl.TicketWizard.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.chernobyl.TicketWizard.Enums.TicketCategory;

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
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ticket_types")
@Getter
@Setter
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "display_name")
    private String displayName;

    private BigDecimal price;

    private Integer totalQuantity;

    private Integer availableQuantity;

    private Integer heldQuantity;

    private Integer soldQuantity;

    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
    private List<Ticket> ticket = new ArrayList<>();

    public TicketType() {
    }

    public TicketType(String displayName, BigDecimal price, Integer totalQuantity, Event event) {
        this.displayName = displayName;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.event = event;
    }
}
