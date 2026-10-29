package com.dormfix.ticket;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ticket endpoints. This class only handles HTTP (paths, status codes); the real work
 * happens in TicketService.
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** GET /api/tickets - all tickets, newest first. */
    @GetMapping("/tickets")
    public List<TicketResponse> getTickets() {
        return ticketService.getAllTickets();
    }

    /** GET /api/tickets/{id} - one ticket, or 404. */
    @GetMapping("/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }

    /** POST /api/tickets - create a ticket. Returns 201 Created with the new ticket. */
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    // These turn the service's exceptions into HTTP responses with a short JSON message.
    // They only apply to requests handled by this controller.

    @ExceptionHandler(InvalidTicketException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTicket(InvalidTicketException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(TicketNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }
}
