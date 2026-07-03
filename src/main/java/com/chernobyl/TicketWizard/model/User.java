package com.chernobyl.TicketWizard.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chernobyl.TicketWizard.Enums.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    private String name;

    private String phone;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private List<Event> organizerEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    public User() {
    }

    public User(String email, String passwordHash, UserRole role, String name, String phone,
            LocalDateTime createdAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
    }
}
