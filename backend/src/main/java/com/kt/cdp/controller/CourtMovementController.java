package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.CourtHearingStatus;
import com.kt.cdp.dto.court.CourtHearingRequest;
import com.kt.cdp.dto.court.CourtHearingResponse;
import com.kt.cdp.dto.court.CourtHearingStatusUpdateRequest;
import com.kt.cdp.service.CourtMovementService;
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
import java.util.List;

@RestController
@RequestMapping("/api/court-movements")
@Tag(name = "Court Movement", description = "Court hearing / court movement management")
public class CourtMovementController {

    private final CourtMovementService courtMovementService;

    public CourtMovementController(CourtMovementService courtMovementService) {
        this.courtMovementService = courtMovementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COURT_READ')")
    @Operation(summary = "Search court hearings (paged)")
    public PageResponse<CourtHearingResponse> search(
            @RequestParam(required = false) Long prisonerId,
            @RequestParam(required = false) CourtHearingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hearingDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hearingDateTo,
            Pageable pageable) {
        return courtMovementService.search(prisonerId, status, hearingDateFrom, hearingDateTo, pageable);
    }

    @GetMapping("/not-returned")
    @PreAuthorize("hasAuthority('COURT_READ')")
    @Operation(summary = "List hearings past their expected return that have not been returned")
    public List<CourtHearingResponse> getNotReturned() {
        return courtMovementService.getNotReturned();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COURT_READ')")
    @Operation(summary = "Get a single court hearing")
    public CourtHearingResponse getById(@PathVariable Long id) {
        return courtMovementService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COURT_WRITE')")
    @Operation(summary = "Create a court hearing")
    public ResponseEntity<CourtHearingResponse> create(@Valid @RequestBody CourtHearingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtMovementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COURT_WRITE')")
    @Operation(summary = "Update a court hearing")
    public CourtHearingResponse update(@PathVariable Long id, @Valid @RequestBody CourtHearingRequest request) {
        return courtMovementService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('COURT_WRITE')")
    @Operation(summary = "Advance a court hearing to the next status (forward-only)")
    public CourtHearingResponse updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody CourtHearingStatusUpdateRequest request) {
        return courtMovementService.updateStatus(id, request.status(), request.outcome(), request.remarks());
    }
}
