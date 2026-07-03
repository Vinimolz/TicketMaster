package com.chernobyl.TicketWizard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.model.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    // Used when displaying an event's available ticket tiers to a buyer,
    // and when the organizer manages the tiers for one of their events.
    List<TicketType> findByEventId(Long eventId);
}
