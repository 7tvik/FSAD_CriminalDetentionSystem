package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.release.ReleaseResponse;
import com.kt.cdp.service.ReleaseDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/releases/diary")
@Tag(name = "Release Diary", description = "Automated daily / weekly / custom release diary")
public class ReleaseDiaryController {

    private final ReleaseDiaryService releaseDiaryService;

    public ReleaseDiaryController(ReleaseDiaryService releaseDiaryService) {
        this.releaseDiaryService = releaseDiaryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RELEASE_READ')")
    @Operation(summary = "Generate the release diary for a mode (DAILY|WEEKLY|CUSTOM) and range")
    public ResponseEntity<PageResponse<ReleaseResponse>> diary(
            @RequestParam(required = false, defaultValue = "DAILY") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(releaseDiaryService.generate(mode, from, to, pageable));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('RELEASE_READ')")
    @Operation(summary = "Export the release diary as CSV")
    public ResponseEntity<String> export(
            @RequestParam(required = false, defaultValue = "DAILY") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ReleaseResponse> diary = releaseDiaryService.generateAll(mode, from, to);
        String csv = CsvSupport.releasesToCsv(diary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"release-diary.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
