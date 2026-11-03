package com.kt.cdp.repository;

import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.entity.InterviewRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface InterviewRequestRepository extends JpaRepository<InterviewRequest, Long>, JpaSpecificationExecutor<InterviewRequest> {

    long countByStatus(InterviewStatus status);

    /** Duplicate detection: same prisoner + visitor + date not already terminal. */
    boolean existsByPrisonerIdAndVisitorNameIgnoreCaseAndRequestedDateAndStatusIn(
            Long prisonerId, String visitorName, LocalDate requestedDate, java.util.Collection<InterviewStatus> statuses);
}
