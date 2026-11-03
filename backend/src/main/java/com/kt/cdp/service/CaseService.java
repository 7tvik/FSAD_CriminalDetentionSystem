package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.dto.caze.CaseMapper;
import com.kt.cdp.dto.caze.CaseRequest;
import com.kt.cdp.dto.caze.CaseResponse;
import com.kt.cdp.dto.caze.CaseSpecifications;
import com.kt.cdp.entity.CaseRecord;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.PrisonerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages criminal cases. A prisoner may have multiple cases; case numbers are globally unique.
 */
@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final PrisonerRepository prisonerRepository;
    private final CaseMapper caseMapper;
    private final AuditService auditService;

    public CaseService(CaseRepository caseRepository, PrisonerRepository prisonerRepository,
                       CaseMapper caseMapper, AuditService auditService) {
        this.caseRepository = caseRepository;
        this.prisonerRepository = prisonerRepository;
        this.caseMapper = caseMapper;
        this.auditService = auditService;
    }

    /** Registers a new case against a prisoner. Rejects a duplicate case number. */
    @Transactional
    public CaseResponse addCaseForPrisoner(Long prisonerId, CaseRequest request) {
        Prisoner prisoner = prisonerRepository.findById(prisonerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", prisonerId));
        if (request.caseNumber() != null && caseRepository.existsByCaseNumber(request.caseNumber())) {
            throw new ConflictException("Case number already exists: " + request.caseNumber());
        }
        CaseRecord caze = caseMapper.toEntity(request);
        caze.setPrisoner(prisoner);
        CaseRecord saved = caseRepository.save(caze);
        auditService.record("CREATE", "CaseRecord", saved.getId());
        return caseMapper.toResponse(saved);
    }

    @Transactional
    public CaseResponse update(Long id, CaseRequest request) {
        CaseRecord caze = caseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Case", id));
        if (request.caseNumber() != null
                && !request.caseNumber().equals(caze.getCaseNumber())
                && caseRepository.existsByCaseNumber(request.caseNumber())) {
            throw new ConflictException("Case number already exists: " + request.caseNumber());
        }
        caseMapper.updateEntity(caze, request);
        CaseRecord saved = caseRepository.save(caze);
        auditService.record("UPDATE", "CaseRecord", saved.getId());
        return caseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CaseResponse getById(Long id) {
        CaseRecord caze = caseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Case", id));
        return caseMapper.toResponse(caze);
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getByPrisoner(Long prisonerId) {
        return caseRepository.findByPrisonerId(prisonerId).stream()
                .map(caseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseResponse> search(String caseNumber, String firNumber, String crimeType,
                                             String court, CaseStatus status, Pageable pageable) {
        Specification<CaseRecord> spec = Specification.allOf(
                CaseSpecifications.caseNumber(caseNumber),
                CaseSpecifications.firNumber(firNumber),
                CaseSpecifications.crimeType(crimeType),
                CaseSpecifications.court(court),
                CaseSpecifications.hasStatus(status));
        Page<CaseResponse> page = caseRepository.findAll(spec, pageable).map(caseMapper::toResponse);
        return PageResponse.of(page);
    }
}
