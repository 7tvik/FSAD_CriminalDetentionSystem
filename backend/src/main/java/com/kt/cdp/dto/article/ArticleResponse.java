package com.kt.cdp.dto.article;

import com.kt.cdp.common.enums.ArticleReturnStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Outbound representation of a surrendered article, with denormalized prisoner display fields. */
public record ArticleResponse(
        Long id,
        Long prisonerId,
        String prisonerName,
        String prisonerNumber,
        String name,
        String category,
        int quantity,
        String description,
        String conditionNote,
        LocalDate receivedDate,
        LocalDate returnedDate,
        ArticleReturnStatus returnStatus,
        String remarks,
        LocalDateTime createdAt
) {}
