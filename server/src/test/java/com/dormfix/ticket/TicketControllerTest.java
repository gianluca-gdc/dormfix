package com.dormfix.ticket;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dormfix.category.Category;
import com.dormfix.category.CategoryRepository;
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
 * (same setup as SeedDataTest), so the seeded resident Sam and the categories exist.
 * MockMvc sends fake HTTP requests without opening a real port.
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

    @Test
    void createdTicketCanBeFetchedById() throws Exception {
        Long samId = findUser("Sam").getId();
        Long heatingId = findCategory("Heating").getId();

        String body = """
                { "residentId": %d, "categoryId": %d, "roomNumber": "214",
                  "description": "Heater not working", "priority": "HIGH" }
                """.formatted(samId, heatingId);

        // POST the new ticket and pull its id out of the 201 response.
        String created = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        // GET it back by id and check every field.
        mockMvc.perform(get("/api/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
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
                .andExpect(jsonPath("$.error").value("description is required"));
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
                .andExpect(jsonPath("$.error").value("no resident with id 9999"));
    }

    @Test
    void unknownTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/tickets/9999"))
                .andExpect(status().isNotFound());
    }

    private User findUser(String name) {
        return users.findAll().stream().filter(u -> u.getName().equals(name)).findFirst().orElseThrow();
    }

    private Category findCategory(String name) {
        return categories.findAll().stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }
}
