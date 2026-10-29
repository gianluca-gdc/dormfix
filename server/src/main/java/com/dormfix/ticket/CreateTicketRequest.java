package com.dormfix.ticket;

/**
 * JSON body for POST /api/tickets. Spring fills this record in from the request body.
 * Any field the client leaves out comes through as null, and TicketService checks for that.
 *
 * priority is a String (not the Priority enum) so a bad value like "URGENT" gets our own
 * clear 400 message instead of a generic JSON parsing error.
 */
public record CreateTicketRequest(
        Long residentId,
        Long categoryId,
        String roomNumber,
        String description,
        String priority) {
}
