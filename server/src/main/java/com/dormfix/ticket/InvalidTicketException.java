package com.dormfix.ticket;

/** Thrown by TicketService when a new ticket's data is missing or wrong. Becomes a 400. */
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
