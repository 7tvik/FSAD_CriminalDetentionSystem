package com.kt.cdp.dto.caze;

import com.kt.cdp.common.enums.CaseStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Inbound payload for creating or updating a case. A missing status defaults to
 * {@link CaseStatus#UNDER_INVESTIGATION} in the mapper.
 */
public record CaseRequest(
        String caseNumber,
        @NotBlank String crimeType,
        String firNumber,
        String crimeDescription,
        String policeStation,
        String court,
        String sections,
        LocalDate arrestDate,
        CaseStatus status,
        String investigatingOfficer,
        String remarks
) {}
