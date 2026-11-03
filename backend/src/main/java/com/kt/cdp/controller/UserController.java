package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.Permissions;
import com.kt.cdp.dto.user.CreateUserRequest;
import com.kt.cdp.dto.user.UpdateUserRequest;
import com.kt.cdp.dto.user.UserDto;
import com.kt.cdp.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Administrative user management and current-user profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.USER_READ + "')")
    @Operation(summary = "List users (paged)")
    public ResponseEntity<PageResponse<UserDto>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.list(pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<UserDto> currentProfile() {
        return ResponseEntity.ok(userService.getCurrentProfile());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.USER_READ + "')")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.USER_WRITE + "')")
    @Operation(summary = "Create a user with assigned roles")
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.USER_WRITE + "')")
    @Operation(summary = "Update a user's email, name and roles")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('" + Permissions.USER_WRITE + "')")
    @Operation(summary = "Activate a user account")
    public ResponseEntity<UserDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('" + Permissions.USER_WRITE + "')")
    @Operation(summary = "Deactivate a user account (cannot deactivate yourself)")
    public ResponseEntity<UserDto> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }
}
