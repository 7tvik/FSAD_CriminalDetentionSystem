package com.kt.cdp.dto.report;

import com.kt.cdp.dto.release.ReleaseResponse;

import java.time.LocalDate;
import java.util.List;

/** Upcoming releases within a rolling window starting today. */
public record UpcomingReleaseReport(
        LocalDate from,
        LocalDate to,
        long total,
        List<ReleaseResponse> releases
) {}
