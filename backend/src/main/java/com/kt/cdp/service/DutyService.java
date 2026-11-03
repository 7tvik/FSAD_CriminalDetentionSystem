package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.duty.DutyRequest;
import com.kt.cdp.dto.duty.DutyResponse;
import com.kt.cdp.entity.DutyRecord;
import com.kt.cdp.entity.Staff;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.mapper.DutyMapper;
import com.kt.cdp.repository.DutyRecordRepository;
import com.kt.cdp.repository.StaffRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.SecurityUtils;
import com.kt.cdp.spec.DutySpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Manages duty-register entries: creation, update, lookup and paged search.
 * Enforces that end time is after start time and that a single staff member
 * has no overlapping duties on the same date.
 */
@Service
public class DutyService {

    private final DutyRecordRepository dutyRecordRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final DutyMapper dutyMapper;
    private final AuditService auditService;

    public DutyService(DutyRecordRepository dutyRecordRepository, StaffRepository staffRepository,
                       UserRepository userRepository, DutyMapper dutyMapper, AuditService auditService) {
        this.dutyRecordRepository = dutyRecordRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.dutyMapper = dutyMapper;
        this.auditService = auditService;
    }

    @Transactional
    public DutyResponse create(DutyRequest request) {
        Staff staff = loadStaff(request.staffId());
        validateTimes(request.startTime(), request.endTime());
        checkOverlap(request.staffId(), request.dutyDate(), request.startTime(), request.endTime(), null);

        DutyRecord duty = new DutyRecord();
        duty.setStaff(staff);
        apply(duty, request);
        SecurityUtils.currentUserId().flatMap(userRepository::findById).ifPresent(duty::setCreatedBy);
        dutyRecordRepository.save(duty);

        auditService.record("CREATE", "DutyRecord", duty.getId());
        return dutyMapper.toResponse(duty);
    }

    @Transactional
    public DutyResponse update(Long id, DutyRequest request) {
        DutyRecord duty = dutyRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DutyRecord", id));
        Staff staff = loadStaff(request.staffId());
        validateTimes(request.startTime(), request.endTime());
        checkOverlap(request.staffId(), request.dutyDate(), request.startTime(), request.endTime(), id);

        duty.setStaff(staff);
        apply(duty, request);
        dutyRecordRepository.save(duty);

        auditService.record("UPDATE", "DutyRecord", duty.getId());
        return dutyMapper.toResponse(duty);
    }

    @Transactional(readOnly = true)
    public DutyResponse getById(Long id) {
        return dutyMapper.toResponse(dutyRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DutyRecord", id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<DutyResponse> search(Long staffId, LocalDate dutyDate, String location, Pageable pageable) {
        Specification<DutyRecord> spec = Specification.where(DutySpecifications.staffId(staffId))
                .and(DutySpecifications.dutyDate(dutyDate))
                .and(DutySpecifications.location(location));
        return PageResponse.of(dutyRecordRepository.findAll(spec, pageable).map(dutyMapper::toResponse));
    }

    private Staff loadStaff(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> ResourceNotFoundException.of("Staff", staffId));
    }

    private void apply(DutyRecord duty, DutyRequest request) {
        duty.setLocation(request.location());
        duty.setPurpose(request.purpose());
        duty.setDutyDate(request.dutyDate());
        duty.setStartTime(request.startTime());
        duty.setEndTime(request.endTime());
        duty.setRemarks(request.remarks());
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("End time must be after start time");
        }
    }

    /**
     * Rejects duties whose time span overlaps an existing duty for the same staff
     * member on the same date. Two intervals overlap when start &lt; otherEnd and
     * otherStart &lt; end (touching boundaries are allowed).
     */
    private void checkOverlap(Long staffId, LocalDate dutyDate, LocalTime startTime, LocalTime endTime, Long excludeId) {
        List<DutyRecord> sameDay = dutyRecordRepository.findByStaffIdAndDutyDate(staffId, dutyDate);
        for (DutyRecord existing : sameDay) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (startTime.isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(endTime)) {
                throw new BusinessRuleException("Overlapping duty");
            }
        }
    }
}
