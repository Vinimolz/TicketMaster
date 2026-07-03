package com.chernobyl.TicketWizard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chernobyl.TicketWizard.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by authentication — login is email-based (email is the unique identifier).
    Optional<User> findByEmail(String email);

    // Used during registration to reject duplicate emails before attempting the INSERT,
    // giving a cleaner error message than a DB constraint violation.
    boolean existsByEmail(String email);
}
