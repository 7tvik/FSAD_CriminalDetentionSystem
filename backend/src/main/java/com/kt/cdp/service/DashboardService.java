package com.kt.cdp.service;

import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.dto.dashboard.DashboardResponse;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.CourtHearingRepository;
import com.kt.cdp.repository.InterviewRequestRepository;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Computes live dashboard KPIs from the domain repositories. */
@Service
public class DashboardService {

    private final PrisonerRepository prisonerRepository;
    private final ReleaseRecordRepository releaseRecordRepository;
    private final InterviewRequestRepository interviewRequestRepository;
    private final CourtHearingRepository courtHearingRepository;
    private final MovementRepository movementRepository;
    private final CaseRepository caseRepository;

    public DashboardService(PrisonerRepository prisonerRepository,
                            ReleaseRecordRepository releaseRecordRepository,
                            InterviewRequestRepository interviewRequestRepository,
                            CourtHearingRepository courtHearingRepository,
                            MovementRepository movementRepository,
                            CaseRepository caseRepository) {
        this.prisonerRepository = prisonerRepository;
        this.releaseRecordRepository = releaseRecordRepository;
        this.interviewRequestRepository = interviewRequestRepository;
        this.courtHearingRepository = courtHearingRepository;
        this.movementRepository = movementRepository;
        this.caseRepository = caseRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDate weekEnd = today.plusDays(7);
        LocalDate calendarMonthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate next30Days = today.plusDays(30);

        long totalPrisoners = prisonerRepository.countByActiveTrue();
        long activePrisoners = prisonerRepository.countByStatusAndActiveTrue(PrisonerStatus.ACTIVE);

        long todaysReleases = releaseRecordRepository
                .countByExpectedReleaseDateBetweenAndStatusNot(today, today, ReleaseStatus.CANCELLED);
        long thisWeekReleases = releaseRecordRepository
                .countByExpectedReleaseDateBetweenAndStatusNot(today, weekEnd, ReleaseStatus.CANCELLED);
        long thisMonthReleases = releaseRecordRepository
                .countByExpectedReleaseDateBetweenAndStatusNot(today, calendarMonthEnd, ReleaseStatus.CANCELLED);
        long upcomingReleases = releaseRecordRepository
                .countByExpectedReleaseDateBetweenAndStatusNot(today, next30Days, ReleaseStatus.CANCELLED);

        long pendingInterviews = interviewRequestRepository.countByStatus(InterviewStatus.PENDING);
        long todaysCourtMovements = courtHearingRepository.countByHearingDate(today);
        long currentlyOut = movementRepository.countByStatus(MovementStatus.OUT);
        long overdueMovements = movementRepository.findOverdue(now).size();

        long activeCases = caseRepository.countByStatus(CaseStatus.UNDER_INVESTIGATION)
                + caseRepository.countByStatus(CaseStatus.UNDER_TRIAL);

        return new DashboardResponse(
                totalPrisoners,
                activePrisoners,
                todaysReleases,
                thisWeekReleases,
                thisMonthReleases,
                upcomingReleases,
                pendingInterviews,
                todaysCourtMovements,
                currentlyOut,
                overdueMovements,
                activeCases
        );
    }
}
