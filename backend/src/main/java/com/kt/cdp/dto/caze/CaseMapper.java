package com.kt.cdp.dto.caze;

import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.entity.CaseRecord;
import com.kt.cdp.entity.Prisoner;
import org.springframework.stereotype.Component;

/** Converts between {@link CaseRecord} entities and case DTOs. */
@Component
public class CaseMapper {

    /**
     * Builds a new case entity from the request. The owning prisoner is set by the service.
     * A missing status defaults to UNDER_INVESTIGATION.
     */
    public CaseRecord toEntity(CaseRequest request) {
        CaseRecord caze = new CaseRecord();
        apply(caze, request);
        caze.setStatus(request.status() != null ? request.status() : CaseStatus.UNDER_INVESTIGATION);
        return caze;
    }

    /** Copies mutable fields from the request onto an existing case, keeping its owner intact. */
    public void updateEntity(CaseRecord existing, CaseRequest request) {
        apply(existing, request);
        if (request.status() != null) {
            existing.setStatus(request.status());
        }
    }

    private void apply(CaseRecord caze, CaseRequest request) {
        caze.setCaseNumber(request.caseNumber());
        caze.setFirNumber(request.firNumber());
        caze.setCrimeType(request.crimeType());
        caze.setCrimeDescription(request.crimeDescription());
        caze.setPoliceStation(request.policeStation());
        caze.setCourt(request.court());
        caze.setSections(request.sections());
        caze.setArrestDate(request.arrestDate());
        caze.setInvestigatingOfficer(request.investigatingOfficer());
        caze.setRemarks(request.remarks());
    }

    public CaseResponse toResponse(CaseRecord caze) {
        Prisoner prisoner = caze.getPrisoner();
        Long prisonerId = prisoner != null ? prisoner.getId() : null;
        String prisonerName = prisoner != null
                ? prisoner.getFirstName() + " " + prisoner.getLastName()
                : null;
        String prisonerNumber = prisoner != null ? prisoner.getPrisonerNumber() : null;
        return new CaseResponse(
                caze.getId(),
                prisonerId,
                prisonerName,
                prisonerNumber,
                caze.getCaseNumber(),
                caze.getFirNumber(),
                caze.getCrimeType(),
                caze.getCrimeDescription(),
                caze.getPoliceStation(),
                caze.getCourt(),
                caze.getSections(),
                caze.getArrestDate(),
                caze.getStatus(),
                caze.getInvestigatingOfficer(),
                caze.getRemarks(),
                caze.getCreatedAt()
        );
    }
}
