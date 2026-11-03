package com.kt.cdp.repository;

import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.entity.CaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CaseRepository extends JpaRepository<CaseRecord, Long>, JpaSpecificationExecutor<CaseRecord> {
    List<CaseRecord> findByPrisonerId(Long prisonerId);
    boolean existsByCaseNumber(String caseNumber);
    long countByStatus(CaseStatus status);
}
