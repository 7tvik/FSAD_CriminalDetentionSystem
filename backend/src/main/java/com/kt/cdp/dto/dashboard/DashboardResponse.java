package com.kt.cdp.dto.dashboard;

/**
 * Aggregated key-performance indicators for the operational dashboard. Every
 * value is computed from live repository data.
 */
public record DashboardResponse(
        long totalPrisoners,
        long activePrisoners,
        long todaysReleases,
        long thisWeekReleases,
        long thisMonthReleases,
        long upcomingReleases,
        long pendingInterviews,
        long todaysCourtMovements,
        long currentlyOut,
        long overdueMovements,
        long activeCases
) {}
