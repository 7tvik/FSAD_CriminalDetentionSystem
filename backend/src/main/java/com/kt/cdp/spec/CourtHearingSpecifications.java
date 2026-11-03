package com.kt.cdp.spec;

import com.kt.cdp.common.enums.CourtHearingStatus;
import com.kt.cdp.entity.CourtHearing;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Reusable {@link Specification} fragments for filtering court hearings. */
public final class CourtHearingSpecifications {

    private CourtHearingSpecifications() {}

    public static Specification<CourtHearing> prisonerId(Long prisonerId) {
        return (root, query, cb) -> prisonerId == null ? null : cb.equal(root.get("prisoner").get("id"), prisonerId);
    }

    public static Specification<CourtHearing> status(CourtHearingStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<CourtHearing> hearingDateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("hearingDate"), from);
    }

    public static Specification<CourtHearing> hearingDateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("hearingDate"), to);
    }
}
