package com.dormfix.ticket;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ticket endpoints. This class only handles HTTP (paths, status codes); the real work
 * happens in TicketService. Errors are turned into responses by ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * GET /api/tickets - all tickets, newest first.
     * GET /api/tickets?residentId=1 - only that resident's tickets ("My Requests").
     */
    @GetMapping("/tickets")
    public List<TicketResponse> getTickets(@RequestParam(required = false) Long residentId) {
        if (residentId == null) {
            return ticketService.getAllTickets();
        }
        return ticketService.getTicketsForResident(residentId);
    }

    /** GET /api/tickets/{id} - one ticket, or 404. */
    @GetMapping("/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }

    /** GET /api/tickets/{id}/history - the ticket's status changes, oldest first. */
    @GetMapping("/tickets/{id}/history")
    public List<StatusUpdateResponse> getHistory(@PathVariable Long id) {
        return ticketService.getHistory(id);
    }

    /** POST /api/tickets - create a ticket. Returns 201 Created with the new ticket. */
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    /** PATCH /api/tickets/{id}/status - change a ticket's status. Returns the updated ticket. */
    @PatchMapping("/tickets/{id}/status")
    public TicketResponse updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return ticketService.updateStatus(id, request);
    }
}
