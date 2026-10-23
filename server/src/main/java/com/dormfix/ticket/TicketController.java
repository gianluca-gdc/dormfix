package com.dormfix.ticket;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ticket endpoints. Phase 2 skeleton — returns hardcoded empty list for now.
 * Real data (JPA + SQLite) comes in Phase 3.
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    @GetMapping("/tickets")
    public List<Object> getTickets() {
        return List.of();
    }
}
