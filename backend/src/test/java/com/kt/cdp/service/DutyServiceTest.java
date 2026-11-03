package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.dto.duty.DutyRequest;
import com.kt.cdp.entity.DutyRecord;
import com.kt.cdp.entity.Staff;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.mapper.DutyMapper;
import com.kt.cdp.repository.DutyRecordRepository;
import com.kt.cdp.repository.StaffRepository;
import com.kt.cdp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for duty-register validation rules. */
@ExtendWith(MockitoExtension.class)
class DutyServiceTest {

    @Mock private DutyRecordRepository dutyRecordRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private UserRepository userRepository;
    @Mock private DutyMapper dutyMapper;
    @Mock private AuditService auditService;

    @InjectMocks private DutyService dutyService;

    private static final LocalDate DATE = LocalDate.of(2026, 3, 1);

    private Staff staff() {
        Staff s = new Staff();
        s.setId(10L);
        s.setStaffNumber("S-10");
        s.setFullName("Guard");
        return s;
    }

    @Test
    void endTimeNotAfterStartThrows() {
        when(staffRepository.findById(10L)).thenReturn(Optional.of(staff()));
        DutyRequest req = new DutyRequest(10L, "Gate", "Watch", DATE,
                LocalTime.of(10, 0), LocalTime.of(9, 0), null);

        assertThatThrownBy(() -> dutyService.create(req))
                .isInstanceOf(BusinessRuleException.class);

        verify(dutyRecordRepository, never()).save(any());
    }

    @Test
    void overlappingDutyThrows() {
        when(staffRepository.findById(10L)).thenReturn(Optional.of(staff()));

        DutyRecord existing = new DutyRecord();
        existing.setId(99L);
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(12, 0));
        when(dutyRecordRepository.findByStaffIdAndDutyDate(10L, DATE)).thenReturn(List.of(existing));

        // New duty 09:00-11:00 overlaps the existing 10:00-12:00.
        DutyRequest req = new DutyRequest(10L, "Gate", "Watch", DATE,
                LocalTime.of(9, 0), LocalTime.of(11, 0), null);

        assertThatThrownBy(() -> dutyService.create(req))
                .isInstanceOf(BusinessRuleException.class);

        verify(dutyRecordRepository, never()).save(any());
    }
}
