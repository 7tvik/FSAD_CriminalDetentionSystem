package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.Permissions;
import com.kt.cdp.dto.notification.NotificationResponse;
import com.kt.cdp.service.NotificationScheduler;
import com.kt.cdp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User-facing alerts and their read state")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationScheduler notificationScheduler;

    public NotificationController(NotificationService notificationService,
                                  NotificationScheduler notificationScheduler) {
        this.notificationService = notificationService;
        this.notificationScheduler = notificationScheduler;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.NOTIFICATION_READ + "')")
    @Operation(summary = "List notifications visible to the current user (paged)")
    public ResponseEntity<PageResponse<NotificationResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(notificationService.listForCurrentUser(pageable));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('" + Permissions.NOTIFICATION_READ + "')")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('" + Permissions.NOTIFICATION_READ + "')")
    @Operation(summary = "Manually trigger daily alert generation (idempotent, de-duplicated)")
    public ResponseEntity<Map<String, Integer>> generate() {
        int created = notificationScheduler.generateAlerts();
        return ResponseEntity.ok(Map.of("created", created));
    }
}
