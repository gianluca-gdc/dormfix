package com.dormfix.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusUpdateRepository extends JpaRepository<StatusUpdate, Long> {
    // One ticket's history, oldest first. Id breaks ties if two updates share a timestamp.
    List<StatusUpdate> findAllByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
