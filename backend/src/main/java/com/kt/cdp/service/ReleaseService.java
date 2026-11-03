package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.common.enums.ReleaseType;
import com.kt.cdp.dto.release.ReleaseMapper;
import com.kt.cdp.dto.release.ReleaseRequest;
import com.kt.cdp.dto.release.ReleaseResponse;
import com.kt.cdp.dto.release.ReleaseStatusUpdateRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.ReleaseRecord;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Release lifecycle service. Owns creation, editing and the release state machine:
 * <pre>
 *   UPCOMING  -> READY, ON_HOLD, CANCELLED
 *   READY     -> RELEASED, ON_HOLD, CANCELLED
 *   ON_HOLD   -> UPCOMING, READY, CANCELLED
 *   RELEASED  -> (terminal)
 *   CANCELLED -> (terminal)
 * </pre>
 * Moving to {@code RELEASED} records the actual release date and flips the prisoner
 * status to {@link PrisonerStatus#RELEASED}.
 */
@Service
public class ReleaseService {

    /** Allowed target states per current state. Absent / empty entries are terminal. */
    private static final Map<ReleaseStatus, Set<ReleaseStatus>> TRANSITIONS = Map.of(
            ReleaseStatus.UPCOMING, Set.of(ReleaseStatus.READY, ReleaseStatus.ON_HOLD, ReleaseStatus.CANCELLED),
            ReleaseStatus.READY, Set.of(ReleaseStatus.RELEASED, ReleaseStatus.ON_HOLD, ReleaseStatus.CANCELLED),
            ReleaseStatus.ON_HOLD, Set.of(ReleaseStatus.UPCOMING, ReleaseStatus.READY, ReleaseStatus.CANCELLED),
            ReleaseStatus.RELEASED, Set.of(),
            ReleaseStatus.CANCELLED, Set.of()
    );

    private final ReleaseRecordRepository releaseRecordRepository;
    private final PrisonerRepository prisonerRepository;
    private final UserRepository userRepository;
    private final ReleaseMapper releaseMapper;
    private final AuditService auditService;

    public ReleaseService(ReleaseRecordRepository releaseRecordRepository,
                          PrisonerRepository prisonerRepository,
                          UserRepository userRepository,
                          ReleaseMapper releaseMapper,
                          AuditService auditService) {
        this.releaseRecordRepository = releaseRecordRepository;
        this.prisonerRepository = prisonerRepository;
        this.userRepository = userRepository;
        this.releaseMapper = releaseMapper;
        this.auditService = auditService;
    }

    @Transactional
    public ReleaseResponse createForPrisoner(Long prisonerId, ReleaseRequest request) {
        Prisoner prisoner = prisonerRepository.findById(prisonerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", prisonerId));

        ReleaseRecord entity = new ReleaseRecord();
        entity.setPrisoner(prisoner);
        entity.setExpectedReleaseDate(request.expectedReleaseDate());
        entity.setReleaseType(request.releaseType() != null ? request.releaseType() : ReleaseType.OTHER);
        entity.setReason(request.reason());
        entity.setStatus(request.status() != null ? request.status() : ReleaseStatus.UPCOMING);
        entity.setRemarks(request.remarks());

        ReleaseRecord saved = releaseRecordRepository.save(entity);
        auditService.record("RELEASE_CREATE", "ReleaseRecord", saved.getId());
        return releaseMapper.toResponse(saved);
    }

    @Transactional
    public ReleaseResponse update(Long id, ReleaseRequest request) {
        ReleaseRecord entity = load(id);
        // Editable attributes only. Status is governed by the state machine via updateStatus.
        entity.setExpectedReleaseDate(request.expectedReleaseDate());
        entity.setReleaseType(request.releaseType() != null ? request.releaseType() : entity.getReleaseType());
        entity.setReason(request.reason());
        entity.setRemarks(request.remarks());

        ReleaseRecord saved = releaseRecordRepository.save(entity);
        auditService.record("RELEASE_UPDATE", "ReleaseRecord", saved.getId());
        return releaseMapper.toResponse(saved);
    }

    @Transactional
    public ReleaseResponse updateStatus(Long id, ReleaseStatusUpdateRequest statusUpdate) {
        ReleaseRecord entity = load(id);
        ReleaseStatus from = entity.getStatus();
        ReleaseStatus to = statusUpdate.status();

        if (from == to) {
            throw new BusinessRuleException("Release is already in status " + to);
        }
        Set<ReleaseStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BusinessRuleException("Invalid release status transition: " + from + " -> " + to);
        }

        entity.setStatus(to);
        if (statusUpdate.remarks() != null) {
            entity.setRemarks(statusUpdate.remarks());
        }
        entity.setAuthorizedBy(currentUser());

        if (to == ReleaseStatus.RELEASED) {
            LocalDate actual = statusUpdate.actualReleaseDate() != null
                    ? statusUpdate.actualReleaseDate() : LocalDate.now();
            entity.setActualReleaseDate(actual);
            Prisoner prisoner = entity.getPrisoner();
            if (prisoner != null) {
                prisoner.setStatus(PrisonerStatus.RELEASED);
                prisonerRepository.save(prisoner);
            }
        }

        ReleaseRecord saved = releaseRecordRepository.save(entity);
        auditService.record("RELEASE_STATUS_CHANGE", "ReleaseRecord", saved.getId(),
                from.name(), to.name());
        return releaseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReleaseResponse getById(Long id) {
        return releaseMapper.toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public List<ReleaseResponse> getByPrisoner(Long prisonerId) {
        if (!prisonerRepository.existsById(prisonerId)) {
            throw ResourceNotFoundException.of("Prisoner", prisonerId);
        }
        return releaseRecordRepository.findByPrisonerId(prisonerId).stream()
                .map(releaseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReleaseResponse> list(Pageable pageable) {
        Page<ReleaseResponse> page = releaseRecordRepository.findAll(pageable)
                .map(releaseMapper::toResponse);
        return PageResponse.of(page);
    }

    private ReleaseRecord load(Long id) {
        return releaseRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ReleaseRecord", id));
    }

    private User currentUser() {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}
