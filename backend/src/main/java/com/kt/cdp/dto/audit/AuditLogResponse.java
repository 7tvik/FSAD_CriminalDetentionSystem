package com.kt.cdp.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String usernameSnapshot,
        String action,
        String entityType,
        String entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        LocalDateTime createdAt
) {}
