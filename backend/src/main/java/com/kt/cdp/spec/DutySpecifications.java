package com.kt.cdp.spec;

import com.kt.cdp.entity.DutyRecord;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Reusable {@link Specification} fragments for filtering the duty register. */
public final class DutySpecifications {

    private DutySpecifications() {}

    public static Specification<DutyRecord> staffId(Long staffId) {
        return (root, query, cb) -> staffId == null ? null : cb.equal(root.get("staff").get("id"), staffId);
    }

    public static Specification<DutyRecord> dutyDate(LocalDate dutyDate) {
        return (root, query, cb) -> dutyDate == null ? null : cb.equal(root.get("dutyDate"), dutyDate);
    }

    public static Specification<DutyRecord> location(String location) {
        return (root, query, cb) -> (location == null || location.isBlank())
                ? null
                : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }
}
