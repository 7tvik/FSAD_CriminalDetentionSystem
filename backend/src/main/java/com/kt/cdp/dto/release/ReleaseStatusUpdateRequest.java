package com.kt.cdp.dto.release;

import com.kt.cdp.common.enums.ReleaseStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request to move a release record to a new {@link ReleaseStatus}. When transitioning
 * to {@code RELEASED} the {@code actualReleaseDate} is recorded (defaulting to today
 * when omitted). The transition is validated against the release state machine.
 */
public record ReleaseStatusUpdateRequest(
        @NotNull ReleaseStatus status,
        LocalDate actualReleaseDate,
        String remarks
) {}
