package com.dormfix;

/** JSON body for every error the API returns, e.g. { "error": "Request not found." }. */
public record ErrorResponse(String error) {
}
