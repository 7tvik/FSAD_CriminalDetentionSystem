package com.kt.cdp.dto.release;

import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.common.enums.ReleaseType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Payload for creating a release record via {@code POST /api/releases}. Carries the
 * target prisoner id in the body (avoiding a nested {@code /prisoners/{id}/releases}
 * route collision) together with the standard {@link ReleaseRequest} attributes.
 */
public record ReleaseCreateRequest(
        @NotNull Long prisonerId,
        @NotNull LocalDate expectedReleaseDate,
        ReleaseType releaseType,
        String reason,
        ReleaseStatus status,
        String remarks
) {
    /** Projects the release attributes (excluding prisonerId) onto a {@link ReleaseRequest}. */
    public ReleaseRequest toReleaseRequest() {
        return new ReleaseRequest(expectedReleaseDate, releaseType, reason, status, remarks);
    }
}
