package com.chernobyl.TicketWizard.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Used to retrieve all tickets belonging to a specific order —
    // for example, to display them to the user after purchase.
    List<Ticket> findByOrderId(Long orderId);

    // Used to validate a ticket at entry (scanning use case, out of v1 scope
    // but the query is already in place for when scanning is implemented).
    // UUID lookup is safe to expose publicly — UUIDs don't reveal sequential IDs.
    Optional<Ticket> findByTicketCode(UUID ticketCode);
}
