package com.kt.cdp.dto.interview;

import com.kt.cdp.entity.InterviewRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.User;
import org.springframework.stereotype.Component;

/** Converts between {@link InterviewRequest} entities and interview DTOs. */
@Component
public class InterviewMapper {

    /**
     * Builds a new entity from the request. The prisoner association is resolved
     * and supplied by the service; status defaults to PENDING on the entity.
     */
    public InterviewRequest toEntity(InterviewRequestDto dto, Prisoner prisoner) {
        InterviewRequest entity = new InterviewRequest();
        entity.setPrisoner(prisoner);
        entity.setVisitorName(dto.visitorName());
        entity.setRelationship(dto.relationship());
        entity.setContact(dto.contact());
        entity.setIdDetails(dto.idDetails());
        entity.setRequestedDate(dto.requestedDate());
        entity.setRequestedTime(dto.requestedTime());
        entity.setPurpose(dto.purpose());
        return entity;
    }

    public InterviewResponse toResponse(InterviewRequest entity) {
        Prisoner prisoner = entity.getPrisoner();
        User reviewer = entity.getReviewedBy();
        return new InterviewResponse(
                entity.getId(),
                prisoner != null ? prisoner.getId() : null,
                prisoner != null ? (prisoner.getFirstName() + " " + prisoner.getLastName()) : null,
                prisoner != null ? prisoner.getPrisonerNumber() : null,
                entity.getVisitorName(),
                entity.getRelationship(),
                entity.getContact(),
                entity.getIdDetails(),
                entity.getRequestedDate(),
                entity.getRequestedTime(),
                entity.getPurpose(),
                entity.getStatus(),
                reviewer != null ? reviewer.getUsername() : null,
                entity.getReviewDate(),
                entity.getRemarks(),
                entity.getCreatedAt()
        );
    }
}
