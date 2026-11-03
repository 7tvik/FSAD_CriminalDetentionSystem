package com.kt.cdp.dto.release;

import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.common.enums.ReleaseType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model for a release record. Prisoner and authorizing-user attributes are
 * denormalized into display fields so the response never nests other domains' DTOs.
 */
public record ReleaseResponse(
        Long id,
        Long prisonerId,
        String prisonerNumber,
        String prisonerName,
        LocalDate expectedReleaseDate,
        ReleaseType releaseType,
        String reason,
        ReleaseStatus status,
        String remarks,
        String authorizedByUsername,
        LocalDate actualReleaseDate,
        LocalDateTime createdAt
) {}
