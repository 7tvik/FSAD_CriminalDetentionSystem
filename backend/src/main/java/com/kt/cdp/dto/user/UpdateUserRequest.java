package com.kt.cdp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Admin-only user update. Password is NOT changed here (use the change-password flow). */
public record UpdateUserRequest(
        @Email String email,
        @Size(max = 120) String fullName,
        Set<String> roles
) {}
