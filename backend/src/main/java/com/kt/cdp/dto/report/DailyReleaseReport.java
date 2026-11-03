package com.kt.cdp.dto.report;

import com.kt.cdp.dto.release.ReleaseResponse;

import java.time.LocalDate;
import java.util.List;

/** Releases expected on a single calendar day (excluding cancelled records). */
public record DailyReleaseReport(
        LocalDate date,
        long total,
        List<ReleaseResponse> releases
) {}
