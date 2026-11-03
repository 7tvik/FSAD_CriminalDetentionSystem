package com.kt.cdp.dto.prisoner;

import com.kt.cdp.common.enums.ArticleReturnStatus;
import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.common.enums.ReleaseType;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregated view of a prisoner's related records. Deliberately self-contained: it embeds
 * lightweight summary rows rather than reusing other domains' DTOs, so the prisoner package
 * stays independent of the case / article / release / movement packages.
 */
public record PrisonerHistoryResponse(
        Long prisonerId,
        String prisonerNumber,
        String prisonerName,
        PrisonerStatus status,
        List<CaseSummary> cases,
        List<ArticleSummary> articles,
        List<ReleaseSummary> releases,
        List<MovementSummary> movements
) {

    public record CaseSummary(
            Long id,
            String caseNumber,
            String crimeType,
            String court,
            CaseStatus status,
            LocalDate arrestDate
    ) {}

    public record ArticleSummary(
            Long id,
            String name,
            String category,
            int quantity,
            ArticleReturnStatus returnStatus,
            LocalDate receivedDate,
            LocalDate returnedDate
    ) {}

    public record ReleaseSummary(
            Long id,
            LocalDate expectedReleaseDate,
            ReleaseType releaseType,
            ReleaseStatus status,
            LocalDate actualReleaseDate
    ) {}

    public record MovementSummary(
            Long id,
            MovementType movementType,
            String destination,
            LocalDate movementDate,
            MovementStatus status
    ) {}
}
