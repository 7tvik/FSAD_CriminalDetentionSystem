package com.kt.cdp.service;

import com.kt.cdp.common.enums.NotificationType;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.ReleaseRecord;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generates daily operational alerts (upcoming releases, overdue movements) as
 * notifications. Runs automatically every morning and can also be triggered
 * manually for demos. All creation is de-duplicated by {@link NotificationService}
 * so repeated runs are idempotent and never spam users.
 */
@Component
public class NotificationScheduler {

    private final ReleaseRecordRepository releaseRecordRepository;
    private final MovementRepository movementRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(ReleaseRecordRepository releaseRecordRepository,
                                 MovementRepository movementRepository,
                                 NotificationService notificationService) {
        this.releaseRecordRepository = releaseRecordRepository;
        this.movementRepository = movementRepository;
        this.notificationService = notificationService;
    }

    /** Daily at 06:00. Delegates to {@link #generateAlerts()}. */
    @Scheduled(cron = "0 0 6 * * *")
    public void generateDailyAlerts() {
        generateAlerts();
    }

    /**
     * Core alert generation, also invoked by the manual trigger endpoint.
     *
     * @return the number of new notifications created
     */
    public int generateAlerts() {
        int created = 0;
        created += generateUpcomingReleaseAlerts();
        created += generateOverdueMovementAlerts();
        return created;
    }

    private int generateUpcomingReleaseAlerts() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<ReleaseRecord> releases =
                releaseRecordRepository.findByExpectedReleaseDateAndStatusNot(tomorrow, ReleaseStatus.CANCELLED);
        int created = 0;
        for (ReleaseRecord release : releases) {
            Prisoner prisoner = release.getPrisoner();
            String name = prisoner != null
                    ? prisoner.getFirstName() + " " + prisoner.getLastName()
                    : "Prisoner";
            String number = prisoner != null ? prisoner.getPrisonerNumber() : "?";
            String title = "Upcoming release: " + name + " (" + number + ")";
            String message = "Release for " + name + " (" + number
                    + ") is scheduled for " + release.getExpectedReleaseDate() + ".";
            if (notificationService.create(NotificationType.RELEASE_UPCOMING, title, message,
                    "ReleaseRecord", release.getId(), null)) {
                created++;
            }
        }
        return created;
    }

    private int generateOverdueMovementAlerts() {
        List<Movement> overdue = movementRepository.findOverdue(LocalDateTime.now());
        int created = 0;
        for (Movement movement : overdue) {
            String title = "Overdue movement: " + movement.getPersonName();
            String message = movement.getPersonName() + " is overdue to return from "
                    + movement.getDestination() + " (expected " + movement.getExpectedReturn() + ").";
            if (notificationService.create(NotificationType.MOVEMENT_OVERDUE, title, message,
                    "Movement", movement.getId(), null)) {
                created++;
            }
        }
        return created;
    }
}
