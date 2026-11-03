package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.CourtHearingStatus;
import com.kt.cdp.dto.court.CourtHearingRequest;
import com.kt.cdp.dto.court.CourtHearingResponse;
import com.kt.cdp.entity.CaseRecord;
import com.kt.cdp.entity.CourtHearing;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.Staff;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.mapper.CourtHearingMapper;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.CourtHearingRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.StaffRepository;
import com.kt.cdp.spec.CourtHearingSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Manages court movements (court hearings) including a forward-only state machine:
 * <pre>
 *   SCHEDULED --&gt; DEPARTED --&gt; AT_COURT --&gt; HEARING_COMPLETED --&gt; DEPARTED_COURT --&gt; RETURNED
 *   SCHEDULED --&gt; CANCELLED
 * </pre>
 * Any other transition is rejected with a {@link BusinessRuleException}.
 */
@Service
public class CourtMovementService {

    private static final Set<CourtHearingStatus> AWAY_STATUSES = EnumSet.of(
            CourtHearingStatus.DEPARTED,
            CourtHearingStatus.AT_COURT,
            CourtHearingStatus.DEPARTED_COURT);

    private final CourtHearingRepository courtHearingRepository;
    private final PrisonerRepository prisonerRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;
    private final CourtHearingMapper courtHearingMapper;
    private final AuditService auditService;

    public CourtMovementService(CourtHearingRepository courtHearingRepository, PrisonerRepository prisonerRepository,
                                CaseRepository caseRepository, StaffRepository staffRepository,
                                CourtHearingMapper courtHearingMapper, AuditService auditService) {
        this.courtHearingRepository = courtHearingRepository;
        this.prisonerRepository = prisonerRepository;
        this.caseRepository = caseRepository;
        this.staffRepository = staffRepository;
        this.courtHearingMapper = courtHearingMapper;
        this.auditService = auditService;
    }

    @Transactional
    public CourtHearingResponse create(CourtHearingRequest request) {
        CourtHearing hearing = new CourtHearing();
        hearing.setPrisoner(loadPrisoner(request.prisonerId()));
        hearing.setCaseRecord(resolveCase(request.caseId()));
        apply(hearing, request);
        hearing.setStatus(CourtHearingStatus.SCHEDULED);
        courtHearingRepository.save(hearing);

        auditService.record("CREATE", "CourtHearing", hearing.getId());
        return courtHearingMapper.toResponse(hearing);
    }

    @Transactional
    public CourtHearingResponse update(Long id, CourtHearingRequest request) {
        CourtHearing hearing = load(id);
        hearing.setPrisoner(loadPrisoner(request.prisonerId()));
        hearing.setCaseRecord(resolveCase(request.caseId()));
        apply(hearing, request);
        courtHearingRepository.save(hearing);

        auditService.record("UPDATE", "CourtHearing", hearing.getId());
        return courtHearingMapper.toResponse(hearing);
    }

    @Transactional(readOnly = true)
    public CourtHearingResponse getById(Long id) {
        return courtHearingMapper.toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<CourtHearingResponse> search(Long prisonerId, CourtHearingStatus status,
                                                     LocalDate hearingDateFrom, LocalDate hearingDateTo,
                                                     Pageable pageable) {
        Specification<CourtHearing> spec = Specification.where(CourtHearingSpecifications.prisonerId(prisonerId))
                .and(CourtHearingSpecifications.status(status))
                .and(CourtHearingSpecifications.hearingDateFrom(hearingDateFrom))
                .and(CourtHearingSpecifications.hearingDateTo(hearingDateTo));
        return PageResponse.of(courtHearingRepository.findAll(spec, pageable).map(courtHearingMapper::toResponse));
    }

    @Transactional
    public CourtHearingResponse updateStatus(Long id, CourtHearingStatus newStatus, String outcome, String remarks) {
        CourtHearing hearing = load(id);
        validateTransition(hearing.getStatus(), newStatus);

        if (newStatus == CourtHearingStatus.DEPARTED && hearing.getDepartureTime() == null) {
            hearing.setDepartureTime(LocalDateTime.now());
        }
        if (newStatus == CourtHearingStatus.RETURNED && hearing.getActualReturn() == null) {
            hearing.setActualReturn(LocalDateTime.now());
        }
        if (outcome != null) {
            hearing.setOutcome(outcome);
        }
        if (remarks != null) {
            hearing.setRemarks(remarks);
        }
        hearing.setStatus(newStatus);
        courtHearingRepository.save(hearing);

        auditService.record("STATUS_" + newStatus, "CourtHearing", hearing.getId());
        return courtHearingMapper.toResponse(hearing);
    }

    @Transactional(readOnly = true)
    public List<CourtHearingResponse> getNotReturned() {
        LocalDateTime now = LocalDateTime.now();
        Specification<CourtHearing> spec = (root, query, cb) -> cb.and(
                root.get("status").in(AWAY_STATUSES),
                cb.isNotNull(root.get("expectedReturn")),
                cb.lessThan(root.get("expectedReturn"), now),
                cb.isNull(root.get("actualReturn")));
        return courtHearingRepository.findAll(spec).stream()
                .map(courtHearingMapper::toResponse)
                .toList();
    }

    private void validateTransition(CourtHearingStatus current, CourtHearingStatus target) {
        boolean allowed = switch (current) {
            case SCHEDULED -> target == CourtHearingStatus.DEPARTED || target == CourtHearingStatus.CANCELLED;
            case DEPARTED -> target == CourtHearingStatus.AT_COURT;
            case AT_COURT -> target == CourtHearingStatus.HEARING_COMPLETED;
            case HEARING_COMPLETED -> target == CourtHearingStatus.DEPARTED_COURT;
            case DEPARTED_COURT -> target == CourtHearingStatus.RETURNED;
            case RETURNED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new BusinessRuleException("Invalid court hearing transition: " + current + " -> " + target);
        }
    }

    private CourtHearing load(Long id) {
        return courtHearingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("CourtHearing", id));
    }

    private Prisoner loadPrisoner(Long prisonerId) {
        return prisonerRepository.findById(prisonerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", prisonerId));
    }

    private CaseRecord resolveCase(Long caseId) {
        if (caseId == null) {
            return null;
        }
        return caseRepository.findById(caseId)
                .orElseThrow(() -> ResourceNotFoundException.of("CaseRecord", caseId));
    }

    private void apply(CourtHearing hearing, CourtHearingRequest request) {
        hearing.setCourt(request.court());
        hearing.setHearingDate(request.hearingDate());
        hearing.setDestination(request.destination());
        hearing.setDepartureTime(request.departureTime());
        hearing.setExpectedReturn(request.expectedReturn());
        hearing.setEscortOfficer(resolveEscort(request.escortOfficerId()));
        hearing.setRemarks(request.remarks());
    }

    private Staff resolveEscort(Long escortOfficerId) {
        if (escortOfficerId == null) {
            return null;
        }
        return staffRepository.findById(escortOfficerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Staff", escortOfficerId));
    }
}
