package com.kt.cdp.spec;

import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.entity.Movement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Reusable {@link Specification} fragments for filtering the movement register. */
public final class MovementSpecifications {

    private MovementSpecifications() {}

    public static Specification<Movement> personRefId(Long personRefId) {
        return (root, query, cb) -> personRefId == null ? null : cb.equal(root.get("personRefId"), personRefId);
    }

    public static Specification<Movement> movementType(MovementType movementType) {
        return (root, query, cb) -> movementType == null ? null : cb.equal(root.get("movementType"), movementType);
    }

    public static Specification<Movement> status(MovementStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Movement> movementDate(LocalDate movementDate) {
        return (root, query, cb) -> movementDate == null ? null : cb.equal(root.get("movementDate"), movementDate);
    }

    public static Specification<Movement> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("movementDate"), from);
    }

    public static Specification<Movement> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("movementDate"), to);
    }
}
