package com.kt.cdp.repository;

import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.entity.ReleaseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReleaseRecordRepository extends JpaRepository<ReleaseRecord, Long> {

    List<ReleaseRecord> findByPrisonerId(Long prisonerId);

    /**
     * Release diary core query: releases whose expected date falls within the range,
     * excluding cancelled records. Used for daily / weekly / custom-range diaries.
     */
    @Query("""
            SELECT r FROM ReleaseRecord r
            WHERE r.expectedReleaseDate BETWEEN :from AND :to
              AND r.status <> com.kt.cdp.common.enums.ReleaseStatus.CANCELLED
            """)
    Page<ReleaseRecord> findDiary(@Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  Pageable pageable);

    long countByExpectedReleaseDateBetweenAndStatusNot(LocalDate from, LocalDate to, ReleaseStatus excluded);

    List<ReleaseRecord> findByExpectedReleaseDateAndStatusNot(LocalDate date, ReleaseStatus excluded);

    List<ReleaseRecord> findByActualReleaseDateBetween(LocalDate from, LocalDate to);
}
