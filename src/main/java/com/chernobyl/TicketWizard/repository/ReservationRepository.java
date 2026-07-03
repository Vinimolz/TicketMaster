package com.chernobyl.TicketWizard.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.Enums.ReservationStatus;
import com.chernobyl.TicketWizard.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Used to display a user's active reservations (e.g., "your current holds").
    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    // Used as a pre-check in the checkout service before attempting the INSERT.
    // Even though the DB partial unique index is the real race-condition guard,
    // this application-level check gives us a cleaner, user-facing error message
    // ("you already have an active reservation for this event") rather than
    // letting a DataIntegrityViolationException bubble up from the constraint.
    // Note: this check is NOT a substitute for the index — it still has TOCTOU exposure.
    Optional<Reservation> findByUserIdAndEventIdAndStatus(
            Long userId, Long eventId, ReservationStatus status);

    // Used by the reservation expiry background job (scheduled task).
    // Finds all HELD reservations whose hold window has passed so they can be
    // transitioned to EXPIRED and their inventory counters released.
    List<Reservation> findAllByStatusAndExpiresAtBefore(
            ReservationStatus status, LocalDateTime now);
}
