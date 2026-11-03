package com.kt.cdp.dto.interview;

import com.kt.cdp.common.enums.InterviewStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Read model for an interview request. Prisoner and reviewer attributes are
 * denormalised into flat display fields so the frontend never depends on other
 * domains' DTOs.
 */
public record InterviewResponse(
        Long id,
        Long prisonerId,
        String prisonerName,
        String prisonerNumber,
        String visitorName,
        String relationship,
        String contact,
        String idDetails,
        LocalDate requestedDate,
        LocalTime requestedTime,
        String purpose,
        InterviewStatus status,
        String reviewedByUsername,
        LocalDateTime reviewDate,
        String remarks,
        LocalDateTime createdAt
) {}
