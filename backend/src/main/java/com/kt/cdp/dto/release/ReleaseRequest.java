package com.kt.cdp.dto.release;

import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.common.enums.ReleaseType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Editable attributes of a release record. The {@code status} defaults to
 * {@link ReleaseStatus#UPCOMING} on creation; status transitions afterwards are
 * driven exclusively through the dedicated status-update endpoint / state machine.
 */
public record ReleaseRequest(
        @NotNull LocalDate expectedReleaseDate,
        ReleaseType releaseType,
        String reason,
        ReleaseStatus status,
        String remarks
) {}
