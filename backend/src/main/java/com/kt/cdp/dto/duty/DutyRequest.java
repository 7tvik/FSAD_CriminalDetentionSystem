package com.kt.cdp.dto.duty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Request payload to create or update a duty-register entry. */
public record DutyRequest(
        @NotNull Long staffId,
        @NotBlank String location,
        @NotBlank String purpose,
        @NotNull LocalDate dutyDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String remarks
) {}
