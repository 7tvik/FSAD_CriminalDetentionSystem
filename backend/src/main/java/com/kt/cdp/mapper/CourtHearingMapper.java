package com.kt.cdp.mapper;

import com.kt.cdp.common.enums.CourtHearingStatus;
import com.kt.cdp.dto.court.CourtHearingResponse;
import com.kt.cdp.entity.CaseRecord;
import com.kt.cdp.entity.CourtHearing;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.Staff;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/** Converts {@link CourtHearing} entities into {@link CourtHearingResponse} DTOs. */
@Component
public class CourtHearingMapper {

    /** Statuses in which the prisoner is away from the facility. */
    private static final Set<CourtHearingStatus> AWAY_STATUSES = EnumSet.of(
            CourtHearingStatus.DEPARTED,
            CourtHearingStatus.AT_COURT,
            CourtHearingStatus.DEPARTED_COURT);

    public CourtHearingResponse toResponse(CourtHearing hearing) {
        Movement movement = hearing.getMovement();
        Prisoner prisoner = hearing.getPrisoner();
        CaseRecord caseRecord = hearing.getCaseRecord();
        Staff escort = hearing.getEscortOfficer();
        return new CourtHearingResponse(
                hearing.getId(),
                movement != null ? movement.getId() : null,
                prisoner != null ? prisoner.getId() : null,
                prisoner != null ? prisoner.getFirstName() + " " + prisoner.getLastName() : null,
                prisoner != null ? prisoner.getPrisonerNumber() : null,
                caseRecord != null ? caseRecord.getId() : null,
                caseRecord != null ? caseRecord.getCaseNumber() : null,
                hearing.getCourt(),
                hearing.getHearingDate(),
                hearing.getDestination(),
                hearing.getDepartureTime(),
                hearing.getExpectedReturn(),
                hearing.getActualReturn(),
                escort != null ? escort.getId() : null,
                escort != null ? escort.getFullName() : null,
                hearing.getStatus(),
                hearing.getOutcome(),
                isNotReturned(hearing),
                hearing.getRemarks(),
                hearing.getCreatedAt()
        );
    }

    /** Derived: away, past expected return, and not yet actually returned. */
    private boolean isNotReturned(CourtHearing hearing) {
        return AWAY_STATUSES.contains(hearing.getStatus())
                && hearing.getExpectedReturn() != null
                && hearing.getExpectedReturn().isBefore(LocalDateTime.now())
                && hearing.getActualReturn() == null;
    }
}
