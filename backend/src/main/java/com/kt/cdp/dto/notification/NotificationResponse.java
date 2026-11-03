package com.kt.cdp.dto.notification;

import com.kt.cdp.common.enums.NotificationType;

import java.time.LocalDateTime;

/** Read model for a user-facing notification / alert. */
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String entityType,
        Long entityId,
        boolean readFlag,
        LocalDateTime createdAt
) {}
