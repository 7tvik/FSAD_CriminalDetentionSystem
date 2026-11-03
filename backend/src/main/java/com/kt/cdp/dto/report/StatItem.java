package com.kt.cdp.dto.report;

/** Generic labelled count used for statistics breakdowns (e.g. counts by status). */
public record StatItem(
        String label,
        long value
) {}
