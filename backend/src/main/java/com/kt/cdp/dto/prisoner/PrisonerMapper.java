package com.kt.cdp.dto.prisoner;

import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.entity.Prisoner;
import org.springframework.stereotype.Component;

/** Converts between {@link Prisoner} entities and prisoner DTOs. */
@Component
public class PrisonerMapper {

    /** Builds a new entity from the request. A missing status defaults to ACTIVE. */
    public Prisoner toEntity(PrisonerRequest request) {
        Prisoner prisoner = new Prisoner();
        apply(prisoner, request);
        prisoner.setStatus(request.status() != null ? request.status() : PrisonerStatus.ACTIVE);
        prisoner.setActive(true);
        return prisoner;
    }

    /** Copies mutable fields from the request onto an existing entity, preserving the active flag. */
    public void updateEntity(Prisoner existing, PrisonerRequest request) {
        apply(existing, request);
        if (request.status() != null) {
            existing.setStatus(request.status());
        }
    }

    private void apply(Prisoner prisoner, PrisonerRequest request) {
        prisoner.setPrisonerNumber(request.prisonerNumber());
        prisoner.setFirstName(request.firstName());
        prisoner.setLastName(request.lastName());
        prisoner.setDateOfBirth(request.dateOfBirth());
        prisoner.setGender(request.gender());
        prisoner.setAddress(request.address());
        prisoner.setEmergencyContact(request.emergencyContact());
        prisoner.setIdType(request.idType());
        prisoner.setIdNumber(request.idNumber());
        prisoner.setAdmissionDate(request.admissionDate());
        prisoner.setFacility(request.facility());
        prisoner.setWard(request.ward());
        prisoner.setCell(request.cell());
        prisoner.setExpectedReleaseDate(request.expectedReleaseDate());
        prisoner.setPhotoUrl(request.photoUrl());
    }

    public PrisonerResponse toResponse(Prisoner prisoner) {
        return new PrisonerResponse(
                prisoner.getId(),
                prisoner.getPrisonerNumber(),
                prisoner.getFirstName(),
                prisoner.getLastName(),
                prisoner.getDateOfBirth(),
                prisoner.getGender(),
                prisoner.getAddress(),
                prisoner.getEmergencyContact(),
                prisoner.getIdType(),
                prisoner.getIdNumber(),
                prisoner.getAdmissionDate(),
                prisoner.getStatus(),
                prisoner.getFacility(),
                prisoner.getWard(),
                prisoner.getCell(),
                prisoner.getExpectedReleaseDate(),
                prisoner.getPhotoUrl(),
                prisoner.isActive(),
                prisoner.getCreatedAt()
        );
    }
}
