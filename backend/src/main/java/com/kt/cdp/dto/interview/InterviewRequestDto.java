package com.kt.cdp.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Editable attributes of an interview (visit) request. The status is not part of
 * the request body: new requests always start as {@code PENDING} and progress
 * exclusively through the dedicated review endpoints / state machine.
 */
public record InterviewRequestDto(
        @NotNull Long prisonerId,
        @NotBlank String visitorName,
        String relationship,
        String contact,
        String idDetails,
        @NotNull LocalDate requestedDate,
        LocalTime requestedTime,
        String purpose
) {}
