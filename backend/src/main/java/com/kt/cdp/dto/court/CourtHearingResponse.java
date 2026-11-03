package com.kt.cdp.dto.court;

import com.kt.cdp.common.enums.CourtHearingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response view of a court-hearing entry with denormalized display fields.
 * {@code notReturned} is derived (never stored): true when the hearing is in an
 * away status, past its expected return and no actual return has been recorded.
 */
public record CourtHearingResponse(
        Long id,
        Long movementId,
        Long prisonerId,
        String prisonerName,
        String prisonerNumber,
        Long caseId,
        String caseNumber,
        String court,
        LocalDate hearingDate,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime expectedReturn,
        LocalDateTime actualReturn,
        Long escortOfficerId,
        String escortOfficerName,
        CourtHearingStatus status,
        String outcome,
        boolean notReturned,
        String remarks,
        LocalDateTime createdAt
) {}
