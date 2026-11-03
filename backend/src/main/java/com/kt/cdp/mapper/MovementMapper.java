package com.kt.cdp.mapper;

import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.dto.movement.MovementResponse;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.entity.Staff;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Converts {@link Movement} entities into {@link MovementResponse} DTOs. */
@Component
public class MovementMapper {

    public MovementResponse toResponse(Movement movement) {
        Staff escort = movement.getEscortOfficer();
        return new MovementResponse(
                movement.getId(),
                movement.getPersonType(),
                movement.getPersonRefId(),
                movement.getPersonName(),
                movement.getMovementType(),
                movement.getDestination(),
                movement.getReason(),
                movement.getMovementDate(),
                movement.getTimeOut(),
                movement.getExpectedReturn(),
                movement.getActualReturn(),
                escort != null ? escort.getId() : null,
                escort != null ? escort.getFullName() : null,
                movement.getStatus(),
                isOverdue(movement),
                movement.getRemarks(),
                movement.getCreatedAt()
        );
    }

    /** Derived OVERDUE predicate: still OUT and past a set expected-return time. */
    private boolean isOverdue(Movement movement) {
        return movement.getStatus() == MovementStatus.OUT
                && movement.getExpectedReturn() != null
                && movement.getExpectedReturn().isBefore(LocalDateTime.now());
    }
}
