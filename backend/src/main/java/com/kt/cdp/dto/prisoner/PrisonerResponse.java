package com.kt.cdp.dto.prisoner;

import com.kt.cdp.common.enums.PrisonerStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Outbound representation of a prisoner. */
public record PrisonerResponse(
        Long id,
        String prisonerNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String emergencyContact,
        String idType,
        String idNumber,
        LocalDate admissionDate,
        PrisonerStatus status,
        String facility,
        String ward,
        String cell,
        LocalDate expectedReleaseDate,
        String photoUrl,
        boolean active,
        LocalDateTime createdAt
) {}
