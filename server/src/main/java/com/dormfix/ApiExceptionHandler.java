package com.dormfix;

import com.dormfix.ticket.InvalidTicketException;
import com.dormfix.ticket.TicketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * One place that turns exceptions into error responses for every controller.
 *
 * When a service throws one of these exceptions, Spring skips the normal response and calls
 * the matching method here instead. Each method picks the status code and returns
 * { "error": "<message>" }, so the frontend always gets errors in the same shape.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Bad input from the client, e.g. "Description is required." -> 400. */
    @ExceptionHandler(InvalidTicketException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTicket(InvalidTicketException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** A ticket id that doesn't exist -> 404 "Request not found." */
    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFound(TicketNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** The request body isn't valid JSON (e.g. a missing bracket) -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException e) {
        return new ErrorResponse("Request body is not valid JSON.");
    }

    /** A URL value has the wrong type, e.g. /api/tickets/abc or ?residentId=abc -> 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse("Invalid value for " + e.getName() + ".");
    }
}
