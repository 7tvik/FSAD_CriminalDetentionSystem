package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.CourtHearingStatus;
import com.kt.cdp.entity.CourtHearing;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.mapper.CourtHearingMapper;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.CourtHearingRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the forward-only court-hearing state machine. */
@ExtendWith(MockitoExtension.class)
class CourtMovementServiceTest {

    @Mock private CourtHearingRepository courtHearingRepository;
    @Mock private PrisonerRepository prisonerRepository;
    @Mock private CaseRepository caseRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private CourtHearingMapper courtHearingMapper;
    @Mock private AuditService auditService;

    @InjectMocks private CourtMovementService courtMovementService;

    @BeforeEach
    void setUp() {
        lenient().when(courtHearingRepository.save(any(CourtHearing.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CourtHearing hearing(CourtHearingStatus status) {
        CourtHearing h = new CourtHearing();
        h.setId(1L);
        h.setStatus(status);
        return h;
    }

    @Test
    void forwardTransitionScheduledToDepartedSucceeds() {
        CourtHearing h = hearing(CourtHearingStatus.SCHEDULED);
        when(courtHearingRepository.findById(1L)).thenReturn(Optional.of(h));

        courtMovementService.updateStatus(1L, CourtHearingStatus.DEPARTED, null, null);

        assertThat(h.getStatus()).isEqualTo(CourtHearingStatus.DEPARTED);
        assertThat(h.getDepartureTime()).isNotNull();
    }

    @Test
    void skippingAStateThrows() {
        CourtHearing h = hearing(CourtHearingStatus.SCHEDULED);
        when(courtHearingRepository.findById(1L)).thenReturn(Optional.of(h));

        // SCHEDULED may only advance to DEPARTED (or CANCELLED); jumping to AT_COURT is illegal.
        assertThatThrownBy(() ->
                courtMovementService.updateStatus(1L, CourtHearingStatus.AT_COURT, null, null))
                .isInstanceOf(BusinessRuleException.class);

        verify(courtHearingRepository, never()).save(any());
    }
}
