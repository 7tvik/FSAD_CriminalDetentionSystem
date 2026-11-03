package com.kt.cdp.service;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.release.ReleaseMapper;
import com.kt.cdp.dto.release.ReleaseResponse;
import com.kt.cdp.entity.ReleaseRecord;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.repository.ReleaseRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Automated release diary engine. Resolves a date range from a mode and generates
 * the diary from {@link ReleaseRecordRepository#findDiary} (which already excludes
 * {@code CANCELLED} records).
 * <p>Range resolution:
 * <ul>
 *   <li>{@code DAILY}  — a single day ({@code from == to}); defaults to today.</li>
 *   <li>{@code WEEKLY} — {@code from}..{@code from + 6 days} (7-day window from the
 *       chosen start date, not a fixed Mon–Sun calendar week); start defaults to today.</li>
 *   <li>{@code CUSTOM} — the provided {@code from}/{@code to} (both default to today);
 *       {@code from > to} is rejected.</li>
 * </ul>
 */
@Service
public class ReleaseDiaryService {

    /** Supported diary modes. */
    public enum DiaryMode { DAILY, WEEKLY, CUSTOM }

    private final ReleaseRecordRepository releaseRecordRepository;
    private final ReleaseMapper releaseMapper;

    public ReleaseDiaryService(ReleaseRecordRepository releaseRecordRepository,
                               ReleaseMapper releaseMapper) {
        this.releaseRecordRepository = releaseRecordRepository;
        this.releaseMapper = releaseMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReleaseResponse> generate(String mode, LocalDate from, LocalDate to, Pageable pageable) {
        DateRange range = resolveRange(mode, from, to);
        Page<ReleaseResponse> page = releaseRecordRepository
                .findDiary(range.from(), range.to(), pageable)
                .map(releaseMapper::toResponse);
        return PageResponse.of(page);
    }

    /** Materializes the whole (unpaged) diary, e.g. for CSV export. */
    @Transactional(readOnly = true)
    public List<ReleaseResponse> generateAll(String mode, LocalDate from, LocalDate to) {
        DateRange range = resolveRange(mode, from, to);
        return releaseRecordRepository
                .findDiary(range.from(), range.to(), Pageable.unpaged())
                .map(releaseMapper::toResponse)
                .getContent();
    }

    private DateRange resolveRange(String mode, LocalDate from, LocalDate to) {
        DiaryMode diaryMode = parseMode(mode);
        LocalDate today = LocalDate.now();
        return switch (diaryMode) {
            case DAILY -> {
                LocalDate day = from != null ? from : today;
                yield new DateRange(day, day);
            }
            case WEEKLY -> {
                LocalDate start = from != null ? from : today;
                yield new DateRange(start, start.plusDays(6));
            }
            case CUSTOM -> {
                LocalDate start = from != null ? from : today;
                LocalDate end = to != null ? to : today;
                if (start.isAfter(end)) {
                    throw new BusinessRuleException("Invalid date range");
                }
                yield new DateRange(start, end);
            }
        };
    }

    private DiaryMode parseMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return DiaryMode.DAILY;
        }
        try {
            return DiaryMode.valueOf(mode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Unsupported diary mode: " + mode);
        }
    }

    private record DateRange(LocalDate from, LocalDate to) {}
}
