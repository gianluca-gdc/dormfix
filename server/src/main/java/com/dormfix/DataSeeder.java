package com.dormfix;

import java.util.List;

import com.dormfix.category.Category;
import com.dormfix.category.CategoryRepository;
import com.dormfix.ticket.Priority;
import com.dormfix.ticket.Ticket;
import com.dormfix.ticket.TicketRepository;
import com.dormfix.ticket.TicketStatus;
import com.dormfix.ticket.StatusUpdateRepository;
import com.dormfix.user.User;
import com.dormfix.user.UserRepository;
import com.dormfix.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedDatabase(CategoryRepository categories, UserRepository users,
            TicketRepository tickets, StatusUpdateRepository statusUpdates) {
        return args -> {
            // Seed the related demo records together so their foreign keys stay valid.
            if (categories.count() != 0 || users.count() != 0 || tickets.count() != 0
                    || statusUpdates.count() != 0) {
                return;
            }

            Category plumbing = categories.save(new Category("Plumbing"));
            Category electrical = categories.save(new Category("Electrical"));
            Category heating = categories.save(new Category("Heating"));
            categories.save(new Category("Furniture"));
            categories.save(new Category("Other"));

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User resident = users.save(new User("Sam", "sam@example.edu", encoder.encode("sam-password"),
                    UserRole.RESIDENT, "214"));
            User staff = users.save(new User("Dana", "dana@example.edu", encoder.encode("dana-password"),
                    UserRole.STAFF, null));

            tickets.saveAll(List.of(
                    new Ticket(resident, plumbing, "214", "The bathroom sink is leaking.", Priority.HIGH,
                            TicketStatus.OPEN),
                    new Ticket(resident, electrical, "214", "The desk lamp outlet is loose.", Priority.MEDIUM,
                            TicketStatus.IN_PROGRESS),
                    new Ticket(resident, heating, "214", "The radiator is working again.", Priority.LOW,
                            TicketStatus.RESOLVED)));
        };
    }
}
