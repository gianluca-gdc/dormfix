package com.dormfix.ticket;

/**
 * JSON body for one entry in GET /api/tickets/{id}/history. Example:
 * { "oldStatus": "OPEN", "newStatus": "IN_PROGRESS", "note": "ordered a new part",
 *   "updatedBy": "Dana", "createdAt": "2026-11-05T14:30:00Z" }
 */
public record StatusUpdateResponse(
        String oldStatus,
        String newStatus,
        String note,
        String updatedBy,
        String createdAt) {

    /** Copies the fields we want to show out of the database entity. */
    public static StatusUpdateResponse from(StatusUpdate update) {
        return new StatusUpdateResponse(
                update.getOldStatus().name(),
                update.getNewStatus().name(),
                // The note is optional, but the frontend expects text, so send "" instead of null.
                update.getNote() == null ? "" : update.getNote(),
                update.getUpdatedBy().getName(),
                TicketResponse.toIsoUtc(update.getCreatedAt()));
    }
}
