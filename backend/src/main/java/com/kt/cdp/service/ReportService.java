package com.kt.cdp.service;

import com.kt.cdp.common.enums.CaseStatus;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.dto.release.ReleaseMapper;
import com.kt.cdp.dto.release.ReleaseResponse;
import com.kt.cdp.dto.report.DailyReleaseReport;
import com.kt.cdp.dto.report.MonthlyReleaseReport;
import com.kt.cdp.dto.report.StatItem;
import com.kt.cdp.dto.report.UpcomingReleaseReport;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

/**
 * Read-only reporting built entirely from live repository data. All release reports
 * exclude {@link ReleaseStatus#CANCELLED} records.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReleaseRecordRepository releaseRecordRepository;
    private final PrisonerRepository prisonerRepository;
    private final CaseRepository caseRepository;
    private final ReleaseMapper releaseMapper;

    public ReportService(ReleaseRecordRepository releaseRecordRepository,
                         PrisonerRepository prisonerRepository,
                         CaseRepository caseRepository,
                         ReleaseMapper releaseMapper) {
        this.releaseRecordRepository = releaseRecordRepository;
        this.prisonerRepository = prisonerRepository;
        this.caseRepository = caseRepository;
        this.releaseMapper = releaseMapper;
    }

    public DailyReleaseReport dailyReleaseReport(LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        List<ReleaseResponse> releases = releaseRecordRepository
                .findByExpectedReleaseDateAndStatusNot(day, ReleaseStatus.CANCELLED).stream()
                .map(releaseMapper::toResponse)
                .toList();
        return new DailyReleaseReport(day, releases.size(), releases);
    }

    public MonthlyReleaseReport monthlyReleaseReport(int year, int month) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("Invalid year/month: " + year + "-" + month);
        }
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        List<ReleaseResponse> releases = releaseRecordRepository
                .findDiary(from, to, Pageable.unpaged())
                .map(releaseMapper::toResponse)
                .getContent();
        return new MonthlyReleaseReport(year, month, releases.size(), releases);
    }

    public UpcomingReleaseReport upcomingReleases(int days) {
        if (days < 0) {
            throw new BusinessRuleException("days must not be negative");
        }
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        List<ReleaseResponse> releases = releaseRecordRepository
                .findDiary(from, to, Pageable.unpaged())
                .map(releaseMapper::toResponse)
                .getContent();
        return new UpcomingReleaseReport(from, to, releases.size(), releases);
    }

    public List<StatItem> prisonerStatistics() {
        return Arrays.stream(PrisonerStatus.values())
                .map(status -> new StatItem(status.name(),
                        prisonerRepository.countByStatusAndActiveTrue(status)))
                .toList();
    }

    public List<StatItem> caseStatistics() {
        return Arrays.stream(CaseStatus.values())
                .map(status -> new StatItem(status.name(), caseRepository.countByStatus(status)))
                .toList();
    }
}
