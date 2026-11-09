package com.dormfix.ticket;

/**
 * JSON body for PATCH /api/tickets/{id}/status, e.g.
 * { "newStatus": "IN_PROGRESS", "actorId": 2, "note": "ordered a new part" }.
 *
 * actorId is the user making the change. note is optional.
 * newStatus is a String (not the enum) so a bad value gets our own clear 400 message.
 */
public record UpdateStatusRequest(String newStatus, Long actorId, String note) {
}
