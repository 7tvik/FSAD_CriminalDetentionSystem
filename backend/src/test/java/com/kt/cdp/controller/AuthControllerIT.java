package com.kt.cdp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.cdp.config.DataInitializer;
import com.kt.cdp.dto.auth.LoginRequest;
import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import com.kt.cdp.repository.RoleRepository;
import com.kt.cdp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for authentication against the full application context on H2.
 * Flyway is disabled in tests, so roles/users are seeded here through the repositories.
 * {@link DataInitializer} is mocked out because it would otherwise fail at startup
 * looking up Flyway-seeded roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean private DataInitializer dataInitializer;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "Test@1234";

    @BeforeEach
    void seedUser() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setName("GOVERNMENT_OFFICIAL");
        role.setDescription("Read-only official");
        Role savedRole = roleRepository.save(role);

        User user = new User();
        user.setUsername(USERNAME);
        user.setEmail("testuser@cdp.local");
        user.setFullName("Test User");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setActive(true);
        user.setRoles(Set.of(savedRole));
        userRepository.save(user);
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequest(USERNAME, PASSWORD));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }

    @Test
    void loginWithBadPasswordReturns401() throws Exception {
        String body = objectMapper.writeValueAsString(new LoginRequest(USERNAME, "WrongPass1"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingProtectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/prisoners"))
                .andExpect(status().isUnauthorized());
    }
}
