package com.kt.cdp.controller;

import com.kt.cdp.dto.report.DailyReleaseReport;
import com.kt.cdp.dto.report.MonthlyReleaseReport;
import com.kt.cdp.dto.report.StatItem;
import com.kt.cdp.dto.report.UpcomingReleaseReport;
import com.kt.cdp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Release reports and prisoner / case statistics")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/releases/daily")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Daily release report (defaults to today)")
    public ResponseEntity<DailyReleaseReport> dailyReleases(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.dailyReleaseReport(date));
    }

    @GetMapping("/releases/monthly")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Monthly release report")
    public ResponseEntity<MonthlyReleaseReport> monthlyReleases(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.monthlyReleaseReport(year, month));
    }

    @GetMapping("/releases/upcoming")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Upcoming releases within the next N days")
    public ResponseEntity<UpcomingReleaseReport> upcomingReleases(
            @RequestParam(required = false, defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.upcomingReleases(days));
    }

    @GetMapping("/statistics/prisoners")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Prisoner counts by status")
    public ResponseEntity<List<StatItem>> prisonerStatistics() {
        return ResponseEntity.ok(reportService.prisonerStatistics());
    }

    @GetMapping("/statistics/cases")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Case counts by status")
    public ResponseEntity<List<StatItem>> caseStatistics() {
        return ResponseEntity.ok(reportService.caseStatistics());
    }

    @GetMapping(value = "/releases/daily/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    @Operation(summary = "Export the daily release report as CSV")
    public ResponseEntity<String> dailyReleasesExport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReleaseReport report = reportService.dailyReleaseReport(date);
        String csv = CsvSupport.releasesToCsv(report.releases());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"daily-release-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
