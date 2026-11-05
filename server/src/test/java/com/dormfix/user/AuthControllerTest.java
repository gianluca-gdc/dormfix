package com.dormfix.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void residentCanLogIn() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"sam@example.edu\", \"password\": \"sam-password\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.name").value("Sam"))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.roomNumber").value("214"));
    }

    @Test
    void staffCanLogIn() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"dana@example.edu\", \"password\": \"dana-password\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dana"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.roomNumber").doesNotExist());
    }

    @Test
    void wrongPasswordReturnsGenericUnauthorizedError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"sam@example.edu\", \"password\": \"wrong-password\" }"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}
