package com.dormfix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dormfix.category.CategoryRepository;
import com.dormfix.ticket.TicketRepository;
import com.dormfix.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
class SeedDataTest {
    @Autowired private CategoryRepository categories;
    @Autowired private UserRepository users;
    @Autowired private TicketRepository tickets;

    @Test
    void startupLoadsDemoSeedDataIntoSeparateInMemoryDatabase() {
        assertEquals(5, categories.count());
        assertEquals(2, users.count());
        assertEquals(3, tickets.count());
    }
}
