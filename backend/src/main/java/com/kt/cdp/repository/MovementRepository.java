package com.kt.cdp.repository;

import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {

    long countByStatus(MovementStatus status);

    long countByMovementTypeAndMovementDate(com.kt.cdp.common.enums.MovementType type, LocalDate date);

    /**
     * Overdue movements: still OUT and past their expected return time.
     * OVERDUE is derived, never stored - this is the single source of that predicate.
     */
    @Query("""
            SELECT m FROM Movement m
            WHERE m.status = com.kt.cdp.common.enums.MovementStatus.OUT
              AND m.expectedReturn IS NOT NULL
              AND m.expectedReturn < :now
            """)
    List<Movement> findOverdue(@Param("now") LocalDateTime now);
}
