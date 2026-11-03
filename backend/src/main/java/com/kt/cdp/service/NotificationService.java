package com.kt.cdp.service;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.NotificationType;
import com.kt.cdp.dto.notification.NotificationResponse;
import com.kt.cdp.entity.Notification;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.NotificationRepository;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creation, retrieval and read-state management of notifications. Creation is
 * de-duplicated on (type, entityId, title) so repeated alert generation does not
 * spam users with identical notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a notification unless an identical one (same type, entity and title)
     * already exists. Returns {@code true} when a new notification was persisted.
     *
     * @param recipientUserId null = broadcast to all users
     */
    @Transactional
    public boolean create(NotificationType type, String title, String message,
                          String entityType, Long entityId, Long recipientUserId) {
        if (notificationRepository.existsByTypeAndEntityIdAndTitle(type, entityId, title)) {
            return false;
        }
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setRecipientUserId(recipientUserId);
        notification.setReadFlag(false);
        notificationRepository.save(notification);
        return true;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listForCurrentUser(Pageable pageable) {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        Page<NotificationResponse> page = notificationRepository.findVisibleToUser(userId, pageable)
                .map(this::toResponse);
        return PageResponse.of(page);
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        notification.setReadFlag(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getEntityType(),
                n.getEntityId(),
                n.isReadFlag(),
                n.getCreatedAt()
        );
    }
}
