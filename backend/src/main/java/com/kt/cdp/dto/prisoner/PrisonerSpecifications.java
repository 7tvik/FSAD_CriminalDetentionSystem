package com.kt.cdp.dto.prisoner;

import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.entity.CaseRecord;
import com.kt.cdp.entity.Prisoner;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Reusable {@link Specification} builders for prisoner search. Each builder returns an
 * always-true predicate when its filter argument is absent, so they compose safely via
 * {@link Specification#allOf}.
 */
public final class PrisonerSpecifications {

    private PrisonerSpecifications() {}

    /** Case-insensitive LIKE across firstName, lastName and prisonerNumber. */
    public static Specification<Prisoner> search(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("prisonerNumber")), like));
        };
    }

    public static Specification<Prisoner> hasStatus(PrisonerStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Prisoner> admittedFrom(LocalDate from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("admissionDate"), from);
    }

    public static Specification<Prisoner> admittedTo(LocalDate to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("admissionDate"), to);
    }

    public static Specification<Prisoner> releaseFrom(LocalDate from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("expectedReleaseDate"), from);
    }

    public static Specification<Prisoner> releaseTo(LocalDate to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("expectedReleaseDate"), to);
    }

    /** When {@code activeOnly} is true, restricts to active prisoners; otherwise no restriction. */
    public static Specification<Prisoner> activeOnly(Boolean activeOnly) {
        return (root, query, cb) -> Boolean.TRUE.equals(activeOnly)
                ? cb.isTrue(root.get("active"))
                : cb.conjunction();
    }

    /**
     * Filters to prisoners linked (via their cases) to a given crime type. Uses a correlated
     * subquery over {@link CaseRecord} so the result stays free of duplicate prisoner rows.
     */
    public static Specification<Prisoner> hasCrimeType(String crimeType) {
        return (root, query, cb) -> {
            if (crimeType == null || crimeType.isBlank()) {
                return cb.conjunction();
            }
            Subquery<Long> sub = query.subquery(Long.class);
            Root<CaseRecord> caseRoot = sub.from(CaseRecord.class);
            sub.select(caseRoot.get("prisoner").get("id"));
            sub.where(cb.equal(cb.lower(caseRoot.get("crimeType")), crimeType.toLowerCase()));
            return root.get("id").in(sub);
        };
    }
}
