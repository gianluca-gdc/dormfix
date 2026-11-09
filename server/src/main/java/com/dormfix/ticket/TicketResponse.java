package com.dormfix.ticket;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * JSON body for one ticket. The frontend is built against these exact field names, so don't
 * rename them. Example:
 * { "id": 1, "residentId": 1, "residentName": "Sam", "category": "Heating", "roomNumber": "214",
 *   "description": "Heater not working", "priority": "HIGH", "status": "OPEN",
 *   "createdAt": "2026-10-20T14:30:00Z" }
 */
public record TicketResponse(
        Long id,
        Long residentId,
        String residentName,
        String category,
        String roomNumber,
        String description,
        String priority,
        String status,
        String createdAt) {

    /** Copies the fields we want to show out of the database entity. */
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getResident().getId(),
                ticket.getResident().getName(),
                ticket.getCategory().getName(),
                ticket.getRoomNumber(),
                ticket.getDescription(),
                ticket.getPriority().name(),
                ticket.getStatus().name(),
                toIsoUtc(ticket.getCreatedAt()));
    }

    /**
     * The entities store times as LocalDateTime (server clock time, no time zone).
     * We treat it as the server's local time, convert to UTC, and drop fractions of a second,
     * which gives the "2026-10-20T14:30:00Z" format the frontend expects.
     */
    static String toIsoUtc(LocalDateTime time) {
        return time
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .truncatedTo(ChronoUnit.SECONDS)
                .toString();
    }
}
