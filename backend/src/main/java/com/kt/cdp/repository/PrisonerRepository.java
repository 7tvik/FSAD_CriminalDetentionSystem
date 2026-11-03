package com.kt.cdp.repository;

import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.entity.Prisoner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrisonerRepository extends JpaRepository<Prisoner, Long>, JpaSpecificationExecutor<Prisoner> {
    boolean existsByPrisonerNumber(String prisonerNumber);
    long countByActiveTrue();
    long countByStatusAndActiveTrue(PrisonerStatus status);
}
