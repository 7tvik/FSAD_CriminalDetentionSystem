package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.dto.caze.CaseRequest;
import com.kt.cdp.dto.caze.CaseResponse;
import com.kt.cdp.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
@Tag(name = "Cases", description = "Criminal case search, retrieval and updates")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CASE_READ')")
    @Operation(summary = "Search cases with optional filters (paged)")
    public ResponseEntity<PageResponse<CaseResponse>> search(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String firNumber,
            @RequestParam(required = false) String crimeType,
            @RequestParam(required = false) String court,
            @RequestParam(required = false) CaseStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(caseService.search(caseNumber, firNumber, crimeType, court, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CASE_READ')")
    @Operation(summary = "Get a case by id")
    public ResponseEntity<CaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CASE_WRITE')")
    @Operation(summary = "Update an existing case")
    public ResponseEntity<CaseResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody CaseRequest request) {
        return ResponseEntity.ok(caseService.update(id, request));
    }
}
