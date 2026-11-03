package com.kt.cdp.repository;

import com.kt.cdp.entity.CourtHearing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface CourtHearingRepository extends JpaRepository<CourtHearing, Long>, JpaSpecificationExecutor<CourtHearing> {
    long countByHearingDate(LocalDate date);
}
