package com.chernobyl.TicketWizard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.Enums.EventStatus;
import com.chernobyl.TicketWizard.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Used by the public event listing — only PUBLISHED events are visible to buyers.
    List<Event> findByStatus(EventStatus status);

    // Used by the organizer dashboard — an organizer can only see/manage their own events.
    List<Event> findByOrganizerId(Long organizerId);

    // Used by the checkout guard to verify the event is both PUBLISHED and within its
    // active sales window (salesStart ≤ now ≤ salesEnd) before allowing a reservation.
    // Passing `now` twice avoids any within-method time drift between the two comparisons.
    List<Event> findByStatusAndSalesStartBeforeAndSalesEndAfter(
            EventStatus status, LocalDateTime salesStart, LocalDateTime salesEnd);
}
