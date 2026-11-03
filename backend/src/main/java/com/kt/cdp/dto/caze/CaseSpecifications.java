package com.kt.cdp.dto.caze;

import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.entity.CaseRecord;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} builders for case search. Each returns an always-true
 * predicate when its argument is absent so they compose safely via {@link Specification#allOf}.
 */
public final class CaseSpecifications {

    private CaseSpecifications() {}

    public static Specification<CaseRecord> caseNumber(String caseNumber) {
        return (root, query, cb) -> (caseNumber == null || caseNumber.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("caseNumber")), "%" + caseNumber.toLowerCase() + "%");
    }

    public static Specification<CaseRecord> firNumber(String firNumber) {
        return (root, query, cb) -> (firNumber == null || firNumber.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("firNumber")), "%" + firNumber.toLowerCase() + "%");
    }

    public static Specification<CaseRecord> crimeType(String crimeType) {
        return (root, query, cb) -> (crimeType == null || crimeType.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("crimeType")), "%" + crimeType.toLowerCase() + "%");
    }

    public static Specification<CaseRecord> court(String court) {
        return (root, query, cb) -> (court == null || court.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("court")), "%" + court.toLowerCase() + "%");
    }

    public static Specification<CaseRecord> hasStatus(CaseStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}
