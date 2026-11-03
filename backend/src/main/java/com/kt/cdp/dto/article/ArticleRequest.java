package com.kt.cdp.dto.article;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Inbound payload for surrendering / updating an article held on a prisoner's behalf. */
public record ArticleRequest(
        @NotBlank String name,
        String category,
        @Min(1) int quantity,
        String description,
        String conditionNote,
        @NotNull LocalDate receivedDate,
        String remarks
) {}
