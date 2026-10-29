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
 * TicketResponse inside the transaction, before it closes.
 */
@Service
public class TicketService {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final CategoryRepository categories;

    public TicketService(TicketRepository tickets, UserRepository users, CategoryRepository categories) {
        this.tickets = tickets;
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

    /** One ticket, or TicketNotFoundException if the id doesn't exist. */
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id) {
        Ticket ticket = tickets.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        return TicketResponse.from(ticket);
    }

    /** Checks the request, saves a new OPEN ticket, and returns it. */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        // 1. Every field is required.
        if (request.residentId() == null) {
            throw new InvalidTicketException("residentId is required");
        }
        if (request.categoryId() == null) {
            throw new InvalidTicketException("categoryId is required");
        }
        if (isBlank(request.roomNumber())) {
            throw new InvalidTicketException("roomNumber is required");
        }
        if (isBlank(request.description())) {
            throw new InvalidTicketException("description is required");
        }
        if (isBlank(request.priority())) {
            throw new InvalidTicketException("priority is required");
        }

        // 2. The ids have to point at real rows, and priority has to be LOW, MEDIUM or HIGH.
        User resident = users.findById(request.residentId())
                .orElseThrow(() -> new InvalidTicketException(
                        "no resident with id " + request.residentId()));
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new InvalidTicketException(
                        "no category with id " + request.categoryId()));
        Priority priority = parsePriority(request.priority());

        // 3. Save it. New tickets always start as OPEN, whatever the client sends.
        Ticket ticket = new Ticket(resident, category, request.roomNumber().trim(),
                request.description().trim(), priority, TicketStatus.OPEN);
        return TicketResponse.from(tickets.save(ticket));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Priority parsePriority(String value) {
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTicketException("priority must be LOW, MEDIUM or HIGH");
        }
    }
}
