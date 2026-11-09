package com.dormfix.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrderByCreatedAtDesc();

    // Spring writes the SQL from the method name: tickets whose resident.id matches, newest first.
    List<Ticket> findAllByResidentIdOrderByCreatedAtDesc(Long residentId);
}
