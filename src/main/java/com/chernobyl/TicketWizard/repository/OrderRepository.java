package com.chernobyl.TicketWizard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Used for the user's order history page.
    List<Order> findByUserId(Long userId);

    // Used to look up the Order that was created from a given Reservation —
    // for example, to return order details after checkout, or during payment webhooks.
    Optional<Order> findByReservationId(Long reservationId);
}
