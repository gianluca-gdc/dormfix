package com.dormfix.ticket;

/** JSON body for 400 and 404 errors, e.g. { "error": "description is required" }. */
public record ErrorResponse(String error) {
}
