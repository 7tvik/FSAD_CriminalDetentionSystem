package com.kt.cdp.controller;

import com.kt.cdp.common.Permissions;
import com.kt.cdp.dto.user.RoleDto;
import com.kt.cdp.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Read-only role and permission catalogue")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_READ + "')")
    @Operation(summary = "List all roles with their permissions")
    public ResponseEntity<List<RoleDto>> list() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + Permissions.ROLE_READ + "')")
    @Operation(summary = "List all permission names")
    public ResponseEntity<List<String>> permissions() {
        return ResponseEntity.ok(roleService.listPermissionNames());
    }
}
