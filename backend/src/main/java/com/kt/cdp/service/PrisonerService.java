package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.PersonType;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.dto.prisoner.PrisonerHistoryResponse;
import com.kt.cdp.dto.prisoner.PrisonerMapper;
import com.kt.cdp.dto.prisoner.PrisonerRequest;
import com.kt.cdp.dto.prisoner.PrisonerResponse;
import com.kt.cdp.dto.prisoner.PrisonerSpecifications;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import com.kt.cdp.repository.SurrenderedArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Manages prisoner master records and assembles the consolidated prisoner history view. */
@Service
public class PrisonerService {

    private final PrisonerRepository prisonerRepository;
    private final CaseRepository caseRepository;
    private final SurrenderedArticleRepository articleRepository;
    private final ReleaseRecordRepository releaseRepository;
    private final MovementRepository movementRepository;
    private final PrisonerMapper prisonerMapper;
    private final AuditService auditService;

    public PrisonerService(PrisonerRepository prisonerRepository, CaseRepository caseRepository,
                           SurrenderedArticleRepository articleRepository, ReleaseRecordRepository releaseRepository,
                           MovementRepository movementRepository, PrisonerMapper prisonerMapper,
                           AuditService auditService) {
        this.prisonerRepository = prisonerRepository;
        this.caseRepository = caseRepository;
        this.articleRepository = articleRepository;
        this.releaseRepository = releaseRepository;
        this.movementRepository = movementRepository;
        this.prisonerMapper = prisonerMapper;
        this.auditService = auditService;
    }

    @Transactional
    public PrisonerResponse create(PrisonerRequest request) {
        if (prisonerRepository.existsByPrisonerNumber(request.prisonerNumber())) {
            throw new ConflictException("Prisoner number already exists: " + request.prisonerNumber());
        }
        validateDates(request);
        Prisoner prisoner = prisonerMapper.toEntity(request);
        Prisoner saved = prisonerRepository.save(prisoner);
        auditService.record("CREATE", "Prisoner", saved.getId());
        return prisonerMapper.toResponse(saved);
    }

    @Transactional
    public PrisonerResponse update(Long id, PrisonerRequest request) {
        Prisoner prisoner = prisonerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", id));
        if (!request.prisonerNumber().equals(prisoner.getPrisonerNumber())
                && prisonerRepository.existsByPrisonerNumber(request.prisonerNumber())) {
            throw new ConflictException("Prisoner number already exists: " + request.prisonerNumber());
        }
        validateDates(request);
        prisonerMapper.updateEntity(prisoner, request);
        Prisoner saved = prisonerRepository.save(prisoner);
        auditService.record("UPDATE", "Prisoner", saved.getId());
        return prisonerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PrisonerResponse getById(Long id) {
        return prisonerMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrisonerResponse> search(String search, PrisonerStatus status, LocalDate admittedFrom,
                                                 LocalDate admittedTo, LocalDate releaseFrom, LocalDate releaseTo,
                                                 Boolean activeOnly, String crimeType, Pageable pageable) {
        Specification<Prisoner> spec = Specification.allOf(
                PrisonerSpecifications.search(search),
                PrisonerSpecifications.hasStatus(status),
                PrisonerSpecifications.admittedFrom(admittedFrom),
                PrisonerSpecifications.admittedTo(admittedTo),
                PrisonerSpecifications.releaseFrom(releaseFrom),
                PrisonerSpecifications.releaseTo(releaseTo),
                PrisonerSpecifications.activeOnly(activeOnly),
                PrisonerSpecifications.hasCrimeType(crimeType));
        Page<PrisonerResponse> page = prisonerRepository.findAll(spec, pageable).map(prisonerMapper::toResponse);
        return PageResponse.of(page);
    }

    /** Soft delete: deactivate and mark INACTIVE without removing the record. */
    @Transactional
    public void softDelete(Long id) {
        Prisoner prisoner = getEntity(id);
        prisoner.setActive(false);
        prisoner.setStatus(PrisonerStatus.INACTIVE);
        prisonerRepository.save(prisoner);
        auditService.record("DELETE", "Prisoner", prisoner.getId());
    }

    /** Consolidated history: the prisoner's cases, held/returned articles, releases and movements. */
    @Transactional(readOnly = true)
    public PrisonerHistoryResponse getHistory(Long id) {
        Prisoner prisoner = getEntity(id);

        List<PrisonerHistoryResponse.CaseSummary> cases = caseRepository.findByPrisonerId(id).stream()
                .map(c -> new PrisonerHistoryResponse.CaseSummary(
                        c.getId(), c.getCaseNumber(), c.getCrimeType(), c.getCourt(),
                        c.getStatus(), c.getArrestDate()))
                .toList();

        List<PrisonerHistoryResponse.ArticleSummary> articles = articleRepository.findByPrisonerId(id).stream()
                .map(a -> new PrisonerHistoryResponse.ArticleSummary(
                        a.getId(), a.getName(), a.getCategory(), a.getQuantity(),
                        a.getReturnStatus(), a.getReceivedDate(), a.getReturnedDate()))
                .toList();

        List<PrisonerHistoryResponse.ReleaseSummary> releases = releaseRepository.findByPrisonerId(id).stream()
                .map(r -> new PrisonerHistoryResponse.ReleaseSummary(
                        r.getId(), r.getExpectedReleaseDate(), r.getReleaseType(),
                        r.getStatus(), r.getActualReleaseDate()))
                .toList();

        Specification<Movement> movementSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("personType"), PersonType.PRISONER),
                cb.equal(root.get("personRefId"), id));
        List<PrisonerHistoryResponse.MovementSummary> movements = movementRepository.findAll(movementSpec).stream()
                .map(m -> new PrisonerHistoryResponse.MovementSummary(
                        m.getId(), m.getMovementType(), m.getDestination(),
                        m.getMovementDate(), m.getStatus()))
                .toList();

        return new PrisonerHistoryResponse(
                prisoner.getId(),
                prisoner.getPrisonerNumber(),
                prisoner.getFirstName() + " " + prisoner.getLastName(),
                prisoner.getStatus(),
                cases,
                articles,
                releases,
                movements);
    }

    private Prisoner getEntity(Long id) {
        return prisonerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", id));
    }

    private void validateDates(PrisonerRequest request) {
        if (request.expectedReleaseDate() != null
                && request.admissionDate() != null
                && request.expectedReleaseDate().isBefore(request.admissionDate())) {
            throw new BusinessRuleException("Expected release date must not be before the admission date");
        }
    }
}
