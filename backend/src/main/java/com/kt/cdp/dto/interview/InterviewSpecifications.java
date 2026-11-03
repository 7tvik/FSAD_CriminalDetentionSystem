package com.kt.cdp.dto.interview;

import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.entity.InterviewRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Reusable {@link Specification} fragments for querying interview requests.
 * Each factory returns {@code null} when its argument is absent so callers can
 * freely combine them; a null spec contributes no predicate.
 */
public final class InterviewSpecifications {

    private InterviewSpecifications() {}

    public static Specification<InterviewRequest> hasPrisonerId(Long prisonerId) {
        if (prisonerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("prisoner").get("id"), prisonerId);
    }

    public static Specification<InterviewRequest> visitorNameLike(String visitorName) {
        if (visitorName == null || visitorName.isBlank()) {
            return null;
        }
        String pattern = "%" + visitorName.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("visitorName")), pattern);
    }

    public static Specification<InterviewRequest> requestedDateFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("requestedDate"), from);
    }

    public static Specification<InterviewRequest> requestedDateTo(LocalDate to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("requestedDate"), to);
    }

    public static Specification<InterviewRequest> hasStatus(InterviewStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<InterviewRequest> statusIn(Collection<InterviewStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("status").in(statuses);
    }
}
