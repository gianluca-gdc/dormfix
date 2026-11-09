package com.dormfix.ticket;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dormfix.category.Category;
import com.dormfix.category.CategoryRepository;
import com.dormfix.user.Role;
import com.dormfix.user.User;
import com.dormfix.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test: starts the whole app against a throwaway in-memory SQLite database
 * (same setup as SeedDataTest), so the seeded resident Sam, staff Dana and the categories exist.
 * MockMvc sends fake HTTP requests without opening a real port.
 *
 * Tests that change a ticket's status create their own ticket first, so they don't depend on
 * the seeded tickets or on each other.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;

    // ----- Creating and reading tickets -----

    @Test
    void createdTicketCanBeFetchedById() throws Exception {
        Long samId = findUser("Sam").getId();
        Integer id = createTicket(samId, "Heater not working");

        mockMvc.perform(get("/api/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.residentId").value(samId))
                .andExpect(jsonPath("$.residentName").value("Sam"))
                .andExpect(jsonPath("$.category").value("Heating"))
                .andExpect(jsonPath("$.roomNumber").value("214"))
                .andExpect(jsonPath("$.description").value("Heater not working"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                // e.g. 2026-10-20T14:30:00Z
                .andExpect(jsonPath("$.createdAt")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")));
    }

    @Test
    void missingDescriptionReturns400() throws Exception {
        String body = """
                { "residentId": %d, "categoryId": %d, "roomNumber": "214", "priority": "HIGH" }
                """.formatted(findUser("Sam").getId(), findCategory("Heating").getId());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Description is required."));
    }

    // TC-07
    @Test
    void unknownCategoryReturns400() throws Exception {
        String body = """
                { "residentId": %d, "categoryId": 9999, "roomNumber": "214",
                  "description": "Heater not working", "priority": "HIGH" }
                """.formatted(findUser("Sam").getId());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Category not found."));
    }

    @Test
    void unknownResidentReturns400() throws Exception {
        String body = """
                { "residentId": 9999, "categoryId": %d, "roomNumber": "214",
                  "description": "Heater not working", "priority": "HIGH" }
                """.formatted(findCategory("Heating").getId());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Resident not found."));
    }

    // TC-16
    @Test
    void unknownTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Request not found."));
    }

    // TC-08
    @Test
    void residentIdFilterOnlyReturnsThatResidentsTickets() throws Exception {
        // A second resident, so there's someone else's ticket that must be left out.
        User alex = users.save(new User("Alex", "alex@example.edu", "not-used", Role.RESIDENT, "101"));
        Integer alexTicketId = createTicket(alex.getId(), "Window won't close");
        Long samId = findUser("Sam").getId();

        // Alex sees only Alex's ticket.
        mockMvc.perform(get("/api/tickets").param("residentId", alex.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(alexTicketId));

        // Sam sees Sam's tickets, and not Alex's.
        mockMvc.perform(get("/api/tickets").param("residentId", samId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].residentId", everyItem(is(samId.intValue()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(alexTicketId))));

        // No parameter still returns everyone's tickets.
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(alexTicketId)));
    }

    // ----- Status changes and history -----

    // TC-15
    @Test
    void staffMovesTicketToInProgressAndHistoryShowsIt() throws Exception {
        Integer id = createTicket(findUser("Sam").getId(), "Shower is cold");
        Long danaId = findUser("Dana").getId();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "newStatus": "IN_PROGRESS", "actorId": %d, "note": "ordered a new part" }
                                """.formatted(danaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/tickets/" + id + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].oldStatus").value("OPEN"))
                .andExpect(jsonPath("$[0].newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].note").value("ordered a new part"))
                .andExpect(jsonPath("$[0].updatedBy").value("Dana"))
                .andExpect(jsonPath("$[0].createdAt")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")));
    }

    @Test
    void staffCannotSkipFromOpenToResolved() throws Exception {
        Integer id = createTicket(findUser("Sam").getId(), "Door lock is stuck");
        Long danaId = findUser("Dana").getId();

        mockMvc.perform(patch("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "newStatus": "RESOLVED", "actorId": %d }
                                """.formatted(danaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("That status change isn't allowed."));

        // The ticket is unchanged and nothing was added to its history.
        mockMvc.perform(get("/api/tickets/" + id))
                .andExpect(jsonPath("$.status").value("OPEN"));
        mockMvc.perform(get("/api/tickets/" + id + "/history"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historyOfUnknownTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/tickets/999/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Request not found."));
    }

    // ----- Helpers -----

    /** POSTs a Heating ticket for the given resident and returns its new id. */
    private Integer createTicket(Long residentId, String description) throws Exception {
        String body = """
                { "residentId": %d, "categoryId": %d, "roomNumber": "214",
                  "description": "%s", "priority": "HIGH" }
                """.formatted(residentId, findCategory("Heating").getId(), description);

        String created = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private User findUser(String name) {
        return users.findAll().stream().filter(u -> u.getName().equals(name)).findFirst().orElseThrow();
    }

    private Category findCategory(String name) {
        return categories.findAll().stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }
}
