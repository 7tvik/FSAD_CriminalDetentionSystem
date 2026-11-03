package com.kt.cdp.dto.duty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Response view of a duty-register entry with denormalized staff display fields. */
public record DutyResponse(
        Long id,
        Long staffId,
        String staffName,
        String staffNumber,
        String location,
        String purpose,
        LocalDate dutyDate,
        LocalTime startTime,
        LocalTime endTime,
        String remarks,
        LocalDateTime createdAt
) {}
