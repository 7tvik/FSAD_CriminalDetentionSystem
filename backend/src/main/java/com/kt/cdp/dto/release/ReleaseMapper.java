package com.kt.cdp.dto.release;

import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.ReleaseRecord;
import com.kt.cdp.entity.User;
import org.springframework.stereotype.Component;

/** Converts {@link ReleaseRecord} entities to their API response representation. */
@Component
public class ReleaseMapper {

    public ReleaseResponse toResponse(ReleaseRecord entity) {
        if (entity == null) {
            return null;
        }
        Prisoner prisoner = entity.getPrisoner();
        User authorizedBy = entity.getAuthorizedBy();
        return new ReleaseResponse(
                entity.getId(),
                prisoner != null ? prisoner.getId() : null,
                prisoner != null ? prisoner.getPrisonerNumber() : null,
                prisoner != null ? fullName(prisoner) : null,
                entity.getExpectedReleaseDate(),
                entity.getReleaseType(),
                entity.getReason(),
                entity.getStatus(),
                entity.getRemarks(),
                authorizedBy != null ? authorizedBy.getUsername() : null,
                entity.getActualReleaseDate(),
                entity.getCreatedAt()
        );
    }

    private String fullName(Prisoner prisoner) {
        return (safe(prisoner.getFirstName()) + " " + safe(prisoner.getLastName())).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
