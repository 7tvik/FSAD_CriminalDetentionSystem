package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.dto.movement.MovementRequest;
import com.kt.cdp.dto.movement.MovementResponse;
import com.kt.cdp.service.MovementService;
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
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movements")
@Tag(name = "Movement Register", description = "In-out movement register management")
public class MovementController {

    private final MovementService movementService;

    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    @Operation(summary = "Search movement entries (paged)")
    public PageResponse<MovementResponse> search(
            @RequestParam(required = false) Long personRefId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) MovementStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable) {
        return movementService.search(personRefId, movementType, status, dateFrom, dateTo, pageable);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    @Operation(summary = "List overdue movements (still OUT past expected return)")
    public List<MovementResponse> getOverdue() {
        return movementService.getOverdue();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    @Operation(summary = "Get a single movement entry")
    public MovementResponse getById(@PathVariable Long id) {
        return movementService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MOVEMENT_WRITE')")
    @Operation(summary = "Create a movement entry")
    public ResponseEntity<MovementResponse> create(@Valid @RequestBody MovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVEMENT_WRITE')")
    @Operation(summary = "Update a movement entry")
    public MovementResponse update(@PathVariable Long id, @Valid @RequestBody MovementRequest request) {
        return movementService.update(id, request);
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAuthority('MOVEMENT_WRITE')")
    @Operation(summary = "Mark a movement as returned (OUT -> RETURNED)")
    public MovementResponse markReturned(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actualReturn) {
        return movementService.markReturned(id, actualReturn);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('MOVEMENT_WRITE')")
    @Operation(summary = "Cancel a movement (SCHEDULED | OUT -> CANCELLED)")
    public MovementResponse cancel(@PathVariable Long id) {
        return movementService.cancel(id);
    }
}
