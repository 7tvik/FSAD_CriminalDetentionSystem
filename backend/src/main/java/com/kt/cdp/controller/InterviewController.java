package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.Permissions;
import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.dto.interview.InterviewRequestDto;
import com.kt.cdp.dto.interview.InterviewResponse;
import com.kt.cdp.dto.interview.InterviewReviewRequest;
import com.kt.cdp.service.InterviewService;
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
@RequestMapping("/api/interviews")
@Tag(name = "Interview Requests", description = "Prisoner visit/interview requests and their review workflow")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_READ + "')")
    @Operation(summary = "Search interview requests (paged, filterable)")
    public ResponseEntity<PageResponse<InterviewResponse>> search(
            @RequestParam(required = false) Long prisonerId,
            @RequestParam(required = false) String visitorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedTo,
            @RequestParam(required = false) InterviewStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(
                interviewService.search(prisonerId, visitorName, requestedFrom, requestedTo, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_READ + "')")
    @Operation(summary = "Get a single interview request by id")
    public ResponseEntity<InterviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_WRITE + "')")
    @Operation(summary = "Create a new interview request (status PENDING)")
    public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.create(request));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_REVIEW + "')")
    @Operation(summary = "Approve a pending interview request")
    public ResponseEntity<InterviewResponse> approve(@PathVariable Long id,
                                                     @RequestBody(required = false) InterviewReviewRequest review) {
        return ResponseEntity.ok(interviewService.approve(id, review));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_REVIEW + "')")
    @Operation(summary = "Reject a pending interview request")
    public ResponseEntity<InterviewResponse> reject(@PathVariable Long id,
                                                    @RequestBody(required = false) InterviewReviewRequest review) {
        return ResponseEntity.ok(interviewService.reject(id, review));
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_REVIEW + "')")
    @Operation(summary = "Schedule an approved interview request")
    public ResponseEntity<InterviewResponse> schedule(@PathVariable Long id,
                                                      @RequestBody(required = false) InterviewReviewRequest review) {
        return ResponseEntity.ok(interviewService.schedule(id, review));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_REVIEW + "')")
    @Operation(summary = "Mark a scheduled interview as completed")
    public ResponseEntity<InterviewResponse> complete(@PathVariable Long id,
                                                      @RequestBody(required = false) InterviewReviewRequest review) {
        return ResponseEntity.ok(interviewService.complete(id, review));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + Permissions.INTERVIEW_REVIEW + "')")
    @Operation(summary = "Cancel an interview request")
    public ResponseEntity<InterviewResponse> cancel(@PathVariable Long id,
                                                    @RequestBody(required = false) InterviewReviewRequest review) {
        return ResponseEntity.ok(interviewService.cancel(id, review));
    }
}
