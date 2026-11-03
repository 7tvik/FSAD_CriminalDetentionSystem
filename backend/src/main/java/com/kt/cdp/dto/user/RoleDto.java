package com.kt.cdp.dto.user;

import java.util.Set;

public record RoleDto(
        Long id,
        String name,
        String description,
        Set<String> permissions
) {}
