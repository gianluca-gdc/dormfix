package com.dormfix.ticket;

/** Thrown by TicketService when no ticket has the requested id. Becomes a 404. */
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long id) {
        super("ticket " + id + " not found");
    }
}
