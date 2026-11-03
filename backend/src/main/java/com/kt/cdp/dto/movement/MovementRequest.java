package com.kt.cdp.dto.movement;

import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.common.enums.PersonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Request payload to create or update an in-out (movement) register entry. */
public record MovementRequest(
        @NotNull PersonType personType,
        Long personRefId,
        @NotBlank String personName,
        @NotNull MovementType movementType,
        String destination,
        String reason,
        @NotNull LocalDate movementDate,
        LocalDateTime timeOut,
        LocalDateTime expectedReturn,
        Long escortOfficerId,
        String remarks
) {}
