package com.kt.cdp.dto.court;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Request payload to create or update a court-hearing (court movement) entry. */
public record CourtHearingRequest(
        @NotNull Long prisonerId,
        Long caseId,
        @NotBlank String court,
        @NotNull LocalDate hearingDate,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime expectedReturn,
        Long escortOfficerId,
        String remarks
) {}
