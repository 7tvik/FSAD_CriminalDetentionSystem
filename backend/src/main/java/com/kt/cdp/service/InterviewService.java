package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.dto.interview.InterviewMapper;
import com.kt.cdp.dto.interview.InterviewRequestDto;
import com.kt.cdp.dto.interview.InterviewResponse;
import com.kt.cdp.dto.interview.InterviewReviewRequest;
import com.kt.cdp.dto.interview.InterviewSpecifications;
import com.kt.cdp.entity.InterviewRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.InterviewRequestRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Business logic and state-machine enforcement for interview (visit) requests.
 *
 * <p>Valid status transitions:
 * <pre>
 *   PENDING   -> APPROVED, REJECTED, CANCELLED
 *   APPROVED  -> SCHEDULED, CANCELLED
 *   SCHEDULED -> COMPLETED, CANCELLED
 *   REJECTED / COMPLETED / CANCELLED -> (terminal)
 * </pre>
 */
@Service
public class InterviewService {

    /** Statuses considered "active" for duplicate detection. */
    private static final Set<InterviewStatus> ACTIVE_STATUSES = Set.of(
            InterviewStatus.PENDING, InterviewStatus.APPROVED, InterviewStatus.SCHEDULED);

    /** Allowed target statuses keyed by current status. */
    private static final Map<InterviewStatus, Set<InterviewStatus>> TRANSITIONS = Map.of(
            InterviewStatus.PENDING, Set.of(InterviewStatus.APPROVED, InterviewStatus.REJECTED, InterviewStatus.CANCELLED),
            InterviewStatus.APPROVED, Set.of(InterviewStatus.SCHEDULED, InterviewStatus.CANCELLED),
            InterviewStatus.SCHEDULED, Set.of(InterviewStatus.COMPLETED, InterviewStatus.CANCELLED),
            InterviewStatus.REJECTED, Set.of(),
            InterviewStatus.COMPLETED, Set.of(),
            InterviewStatus.CANCELLED, Set.of());

    private final InterviewRequestRepository interviewRequestRepository;
    private final PrisonerRepository prisonerRepository;
    private final UserRepository userRepository;
    private final InterviewMapper interviewMapper;
    private final AuditService auditService;

    public InterviewService(InterviewRequestRepository interviewRequestRepository,
                            PrisonerRepository prisonerRepository,
                            UserRepository userRepository,
                            InterviewMapper interviewMapper,
                            AuditService auditService) {
        this.interviewRequestRepository = interviewRequestRepository;
        this.prisonerRepository = prisonerRepository;
        this.userRepository = userRepository;
        this.interviewMapper = interviewMapper;
        this.auditService = auditService;
    }

    @Transactional
    public InterviewResponse create(InterviewRequestDto request) {
        Prisoner prisoner = prisonerRepository.findById(request.prisonerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", request.prisonerId()));

        boolean duplicate = interviewRequestRepository
                .existsByPrisonerIdAndVisitorNameIgnoreCaseAndRequestedDateAndStatusIn(
                        prisoner.getId(), request.visitorName(), request.requestedDate(), ACTIVE_STATUSES);
        if (duplicate) {
            throw new ConflictException("An active interview request already exists for this visitor, prisoner and date");
        }

        InterviewRequest entity = interviewMapper.toEntity(request, prisoner);
        entity.setStatus(InterviewStatus.PENDING);
        InterviewRequest saved = interviewRequestRepository.save(entity);

        auditService.record("CREATE", "InterviewRequest", saved.getId());
        return interviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getById(Long id) {
        return interviewMapper.toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<InterviewResponse> search(Long prisonerId, String visitorName,
                                                  LocalDate requestedFrom, LocalDate requestedTo,
                                                  InterviewStatus status, Pageable pageable) {
        Specification<InterviewRequest> spec = Specification
                .where(InterviewSpecifications.hasPrisonerId(prisonerId))
                .and(InterviewSpecifications.visitorNameLike(visitorName))
                .and(InterviewSpecifications.requestedDateFrom(requestedFrom))
                .and(InterviewSpecifications.requestedDateTo(requestedTo))
                .and(InterviewSpecifications.hasStatus(status));
        Page<InterviewResponse> page = interviewRequestRepository.findAll(spec, pageable)
                .map(interviewMapper::toResponse);
        return PageResponse.of(page);
    }

    @Transactional
    public InterviewResponse approve(Long id, InterviewReviewRequest review) {
        return transition(id, InterviewStatus.APPROVED, review, true);
    }

    @Transactional
    public InterviewResponse reject(Long id, InterviewReviewRequest review) {
        return transition(id, InterviewStatus.REJECTED, review, true);
    }

    @Transactional
    public InterviewResponse schedule(Long id, InterviewReviewRequest review) {
        return transition(id, InterviewStatus.SCHEDULED, review, true);
    }

    @Transactional
    public InterviewResponse complete(Long id, InterviewReviewRequest review) {
        return transition(id, InterviewStatus.COMPLETED, review, false);
    }

    @Transactional
    public InterviewResponse cancel(Long id, InterviewReviewRequest review) {
        return transition(id, InterviewStatus.CANCELLED, review, false);
    }

    private InterviewResponse transition(Long id, InterviewStatus target,
                                         InterviewReviewRequest review, boolean setReviewer) {
        InterviewRequest entity = load(id);
        InterviewStatus current = entity.getStatus();
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    "Invalid interview status transition: " + current + " -> " + target);
        }

        entity.setStatus(target);
        if (review != null && review.remarks() != null && !review.remarks().isBlank()) {
            entity.setRemarks(review.remarks());
        }
        if (setReviewer) {
            entity.setReviewedBy(currentUser());
            entity.setReviewDate(LocalDateTime.now());
        }
        InterviewRequest saved = interviewRequestRepository.save(entity);

        auditService.record("STATUS_CHANGE", "InterviewRequest", saved.getId(),
                current.name(), target.name());
        return interviewMapper.toResponse(saved);
    }

    private InterviewRequest load(Long id) {
        return interviewRequestRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("InterviewRequest", id));
    }

    private User currentUser() {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}
