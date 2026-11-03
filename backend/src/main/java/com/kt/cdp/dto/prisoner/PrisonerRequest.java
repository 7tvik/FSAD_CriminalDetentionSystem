package com.kt.cdp.dto.prisoner;

import com.kt.cdp.common.enums.PrisonerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Inbound payload for creating or updating a prisoner. {@code status} may be omitted,
 * in which case the mapper defaults it to {@link PrisonerStatus#ACTIVE}.
 */
public record PrisonerRequest(
        @NotBlank String prisonerNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String emergencyContact,
        String idType,
        String idNumber,
        @NotNull LocalDate admissionDate,
        PrisonerStatus status,
        String facility,
        String ward,
        String cell,
        LocalDate expectedReleaseDate,
        String photoUrl
) {}
