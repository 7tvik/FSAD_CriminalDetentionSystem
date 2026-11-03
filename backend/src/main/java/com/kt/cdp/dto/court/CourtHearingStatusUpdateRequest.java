package com.kt.cdp.dto.court;

import com.kt.cdp.common.enums.CourtHearingStatus;
import jakarta.validation.constraints.NotNull;

/** Body for the court-hearing status transition endpoint. */
public record CourtHearingStatusUpdateRequest(
        @NotNull CourtHearingStatus status,
        String outcome,
        String remarks
) {}
