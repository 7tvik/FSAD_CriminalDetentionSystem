package com.kt.cdp.repository;

import com.kt.cdp.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Notifications visible to a user: their own plus broadcasts (null recipient). */
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId OR n.recipientUserId IS NULL")
    Page<Notification> findVisibleToUser(@Param("userId") Long userId, Pageable pageable);

    boolean existsByTypeAndEntityIdAndTitle(com.kt.cdp.common.enums.NotificationType type, Long entityId, String title);
}
