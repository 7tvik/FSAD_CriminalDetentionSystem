package com.kt.cdp.dto.user;

import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        boolean active,
        Set<String> roles
) {}
