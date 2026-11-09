package com.dormfix.ticket;

import java.util.List;

import com.dormfix.category.Category;
import com.dormfix.category.CategoryRepository;
import com.dormfix.user.User;
import com.dormfix.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ticket logic lives here so the controller only has to deal with HTTP.
 *
 * Every method is @Transactional because Ticket loads its resident and category lazily
 * (only when asked). Those lookups need an open database session, so we build the
 * responses inside the transaction, before it closes.
 *
 * The error messages below match our test plan word for word, so don't change them.
 */
@Service
public class TicketService {

    private final TicketRepository tickets;
    private final StatusUpdateRepository statusUpdates;
    private final UserRepository users;
    private final CategoryRepository categories;

    public TicketService(TicketRepository tickets, StatusUpdateRepository statusUpdates,
            UserRepository users, CategoryRepository categories) {
        this.tickets = tickets;
        this.statusUpdates = statusUpdates;
        this.users = users;
        this.categories = categories;
    }

    /** All tickets, newest first. */
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return tickets.findAllByOrderByCreatedAtDesc().stream()
                .map(TicketResponse::from)
                .toList();
    }

    /** One resident's tickets ("My Requests"), newest first. Unknown ids just give an empty list. */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForResident(Long residentId) {
        return tickets.findAllByResidentIdOrderByCreatedAtDesc(residentId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    /** One ticket, or 404 "Request not found." */
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id) {
        return TicketResponse.from(findTicket(id));
    }

    /** A ticket's status changes, oldest first, or 404 if the ticket doesn't exist. */
    @Transactional(readOnly = true)
    public List<StatusUpdateResponse> getHistory(Long ticketId) {
        findTicket(ticketId); // only here to throw the 404
        return statusUpdates.findAllByTicketIdOrderByCreatedAtAscIdAsc(ticketId).stream()
                .map(StatusUpdateResponse::from)
                .toList();
    }

    /** Checks the request, saves a new OPEN ticket, and returns it. */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        // 1. Every field is required.
        if (request.residentId() == null) {
            throw new InvalidTicketException("Resident is required.");
        }
        if (request.categoryId() == null) {
            throw new InvalidTicketException("Category is required.");
        }
        if (isBlank(request.roomNumber())) {
            throw new InvalidTicketException("Room number is required.");
        }
        if (isBlank(request.description())) {
            throw new InvalidTicketException("Description is required.");
        }
        if (isBlank(request.priority())) {
            throw new InvalidTicketException("Priority is required.");
        }

        // 2. The ids have to point at real rows, and priority has to be LOW, MEDIUM or HIGH.
        User resident = users.findById(request.residentId())
                .orElseThrow(() -> new InvalidTicketException("Resident not found."));
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new InvalidTicketException("Category not found."));
        Priority priority = parsePriority(request.priority());

        // 3. Save it. New tickets always start as OPEN, whatever the client sends.
        Ticket ticket = new Ticket(resident, category, request.roomNumber().trim(),
                request.description().trim(), priority, TicketStatus.OPEN);
        return TicketResponse.from(tickets.save(ticket));
    }

    /**
     * Changes a ticket's status if StatusRules allows it, and records the change in the history.
     *
     * Both database writes (the ticket and the StatusUpdates row) happen in one transaction:
     * if anything fails, neither is saved, so the history can't get out of sync with the ticket.
     */
    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateStatusRequest request) {
        Ticket ticket = findTicket(ticketId);

        // 1. Check the request.
        if (request.actorId() == null) {
            throw new InvalidTicketException("Actor is required.");
        }
        if (isBlank(request.newStatus())) {
            throw new InvalidTicketException("New status is required.");
        }
        User actor = users.findById(request.actorId())
                .orElseThrow(() -> new InvalidTicketException("User not found."));
        TicketStatus newStatus = parseStatus(request.newStatus());

        // 2. Ask StatusRules (Lakshay's class) whether this person may make this change.
        TicketStatus oldStatus = ticket.getStatus();
        boolean actorIsOwner = actor.getId().equals(ticket.getResident().getId());
        if (!StatusRules.isAllowed(oldStatus, newStatus, actor.getRole(), actorIsOwner)) {
            throw new InvalidTicketException("That status change isn't allowed.");
        }

        // 3. Change the ticket (this also sets updated_at) and write the history row.
        ticket.changeStatus(newStatus);
        String note = isBlank(request.note()) ? null : request.note().trim();
        statusUpdates.save(new StatusUpdate(ticket, actor, oldStatus, newStatus, note));

        // No tickets.save(ticket) needed: the ticket was loaded in this transaction, so JPA
        // notices the change and writes it to the database when the method finishes.
        return TicketResponse.from(ticket);
    }

    private Ticket findTicket(Long id) {
        return tickets.findById(id).orElseThrow(TicketNotFoundException::new);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Priority parsePriority(String value) {
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTicketException("Priority must be LOW, MEDIUM or HIGH.");
        }
    }

    private static TicketStatus parseStatus(String value) {
        try {
            return TicketStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTicketException("Status must be OPEN, IN_PROGRESS or RESOLVED.");
        }
    }
}
