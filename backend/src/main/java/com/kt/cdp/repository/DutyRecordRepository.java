package com.kt.cdp.repository;

import com.kt.cdp.entity.DutyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface DutyRecordRepository extends JpaRepository<DutyRecord, Long>, JpaSpecificationExecutor<DutyRecord> {
    /** Used to detect overlapping duties for the same staff member on the same date. */
    List<DutyRecord> findByStaffIdAndDutyDate(Long staffId, LocalDate dutyDate);
}
