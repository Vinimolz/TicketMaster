package com.chernobyl.TicketWizard.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chernobyl.TicketWizard.Enums.EventStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String venue;

    private LocalDateTime date;

    private LocalDateTime salesStart;

    private LocalDateTime salesEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<TicketType> ticketTypes = new ArrayList<>();

    public Event() {
    }

    public Event(String name, String description, String venue, LocalDateTime date, LocalDateTime salesStart,
            LocalDateTime salesEnd, User organizer) {
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.date = date;
        this.salesStart = salesStart;
        this.salesEnd = salesEnd;
        this.organizer = organizer;
    }
}
