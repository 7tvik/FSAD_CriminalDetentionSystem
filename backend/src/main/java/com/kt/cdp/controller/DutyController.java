package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.duty.DutyRequest;
import com.kt.cdp.dto.duty.DutyResponse;
import com.kt.cdp.service.DutyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/duties")
@Tag(name = "Duty Register", description = "Staff duty roster management")
public class DutyController {

    private final DutyService dutyService;

    public DutyController(DutyService dutyService) {
        this.dutyService = dutyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DUTY_READ')")
    @Operation(summary = "Search duty-register entries (paged)")
    public PageResponse<DutyResponse> search(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dutyDate,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        return dutyService.search(staffId, dutyDate, location, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DUTY_READ')")
    @Operation(summary = "Get a single duty-register entry")
    public DutyResponse getById(@PathVariable Long id) {
        return dutyService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DUTY_WRITE')")
    @Operation(summary = "Create a duty-register entry")
    public ResponseEntity<DutyResponse> create(@Valid @RequestBody DutyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dutyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DUTY_WRITE')")
    @Operation(summary = "Update a duty-register entry")
    public DutyResponse update(@PathVariable Long id, @Valid @RequestBody DutyRequest request) {
        return dutyService.update(id, request);
    }
}
