package com.kt.cdp.dto.report;

import com.kt.cdp.dto.release.ReleaseResponse;

import java.util.List;

/** Releases expected within a given month (excluding cancelled records). */
public record MonthlyReleaseReport(
        int year,
        int month,
        long total,
        List<ReleaseResponse> releases
) {}
