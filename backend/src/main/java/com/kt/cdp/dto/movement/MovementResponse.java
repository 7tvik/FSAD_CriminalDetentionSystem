package com.kt.cdp.dto.movement;

import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.common.enums.PersonType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response view of a movement register entry. {@code overdue} is derived
 * (never stored): true when the record is still OUT and past its expected return.
 */
public record MovementResponse(
        Long id,
        PersonType personType,
        Long personRefId,
        String personName,
        MovementType movementType,
        String destination,
        String reason,
        LocalDate movementDate,
        LocalDateTime timeOut,
        LocalDateTime expectedReturn,
        LocalDateTime actualReturn,
        Long escortOfficerId,
        String escortOfficerName,
        MovementStatus status,
        boolean overdue,
        String remarks,
        LocalDateTime createdAt
) {}
