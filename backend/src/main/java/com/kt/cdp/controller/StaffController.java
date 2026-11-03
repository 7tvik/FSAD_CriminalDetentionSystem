package com.kt.cdp.controller;

import com.kt.cdp.common.Permissions;
import com.kt.cdp.dto.user.StaffDto;
import com.kt.cdp.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@Tag(name = "Staff", description = "Read-only staff directory used by duty and movement forms")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.DUTY_READ + "')")
    @Operation(summary = "List all active staff members")
    public ResponseEntity<List<StaffDto>> list() {
        return ResponseEntity.ok(staffService.listActive());
    }
}
