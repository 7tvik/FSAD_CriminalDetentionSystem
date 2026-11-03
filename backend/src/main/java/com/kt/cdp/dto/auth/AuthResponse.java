package com.kt.cdp.dto.auth;

import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String fullName,
        Set<String> roles,
        Set<String> permissions
) {}
