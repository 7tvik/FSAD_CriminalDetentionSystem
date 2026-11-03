package com.kt.cdp.dto.caze;

import com.kt.cdp.common.enums.CaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Outbound representation of a case, with denormalized prisoner display fields. */
public record CaseResponse(
        Long id,
        Long prisonerId,
        String prisonerName,
        String prisonerNumber,
        String caseNumber,
        String firNumber,
        String crimeType,
        String crimeDescription,
        String policeStation,
        String court,
        String sections,
        LocalDate arrestDate,
        CaseStatus status,
        String investigatingOfficer,
        String remarks,
        LocalDateTime createdAt
) {}
