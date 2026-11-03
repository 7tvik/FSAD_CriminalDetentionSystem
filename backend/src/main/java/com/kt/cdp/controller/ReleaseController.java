package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.Permissions;
import com.kt.cdp.dto.release.ReleaseCreateRequest;
import com.kt.cdp.dto.release.ReleaseRequest;
import com.kt.cdp.dto.release.ReleaseResponse;
import com.kt.cdp.dto.release.ReleaseStatusUpdateRequest;
import com.kt.cdp.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
@Tag(name = "Releases", description = "Release records and their lifecycle")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RELEASE_READ')")
    @Operation(summary = "List release records (paged)")
    public ResponseEntity<PageResponse<ReleaseResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(releaseService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RELEASE_READ')")
    @Operation(summary = "Get a release record by id")
    public ResponseEntity<ReleaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(releaseService.getById(id));
    }

    @GetMapping("/prisoners/{prisonerId}")
    @PreAuthorize("hasAuthority('RELEASE_READ')")
    @Operation(summary = "List release records for a prisoner")
    public ResponseEntity<List<ReleaseResponse>> getByPrisoner(@PathVariable Long prisonerId) {
        return ResponseEntity.ok(releaseService.getByPrisoner(prisonerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RELEASE_WRITE')")
    @Operation(summary = "Create a release record for a prisoner")
    public ResponseEntity<ReleaseResponse> create(@Valid @RequestBody ReleaseCreateRequest request) {
        ReleaseResponse created = releaseService.createForPrisoner(request.prisonerId(), request.toReleaseRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RELEASE_WRITE')")
    @Operation(summary = "Update the editable attributes of a release record")
    public ResponseEntity<ReleaseResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ReleaseRequest request) {
        return ResponseEntity.ok(releaseService.update(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('RELEASE_WRITE')")
    @Operation(summary = "Transition a release record to a new status (state machine enforced)")
    public ResponseEntity<ReleaseResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody ReleaseStatusUpdateRequest request) {
        return ResponseEntity.ok(releaseService.updateStatus(id, request));
    }
}
