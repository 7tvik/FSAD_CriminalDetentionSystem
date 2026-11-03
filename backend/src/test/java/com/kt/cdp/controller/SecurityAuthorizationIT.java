package com.kt.cdp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.cdp.config.DataInitializer;
import com.kt.cdp.dto.prisoner.PrisonerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies method-level authorization on a write endpoint. Uses {@link WithMockUser}
 * to supply authorities directly, avoiding JWT plumbing. {@link DataInitializer} is
 * mocked out so the context can start without Flyway-seeded roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DataInitializer dataInitializer;

    private String prisonerJson() throws Exception {
        return objectMapper.writeValueAsString(new PrisonerRequest(
                "P-SEC-1", "Sam", "Prisoner", LocalDate.of(1985, 6, 15),
                "M", "addr", "contact", "PASSPORT", "ID-9",
                LocalDate.of(2026, 1, 1), null, "Facility", "W1", "C1", null, null));
    }

    @Test
    @WithMockUser(authorities = {"PRISONER_READ"})
    void postPrisonerWithoutWritePermissionIsForbidden() throws Exception {
        mockMvc.perform(post("/api/prisoners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prisonerJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"PRISONER_WRITE"})
    void postPrisonerWithWritePermissionIsPermitted() throws Exception {
        mockMvc.perform(post("/api/prisoners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prisonerJson()))
                .andExpect(status().isCreated());
    }
}
