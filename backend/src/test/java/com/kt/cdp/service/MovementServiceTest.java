package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.mapper.MovementMapper;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the movement (in-out register) state machine and overdue lookup. */
@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock private MovementRepository movementRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private MovementMapper movementMapper;
    @Mock private AuditService auditService;

    @InjectMocks private MovementService movementService;

    @BeforeEach
    void setUp() {
        lenient().when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Movement movement(MovementStatus status) {
        Movement m = new Movement();
        m.setId(1L);
        m.setStatus(status);
        return m;
    }

    @Test
    void markReturnedWhenOutSucceeds() {
        Movement m = movement(MovementStatus.OUT);
        when(movementRepository.findById(1L)).thenReturn(Optional.of(m));
        LocalDateTime returnTime = LocalDateTime.of(2026, 1, 2, 12, 0);

        movementService.markReturned(1L, returnTime);

        assertThat(m.getStatus()).isEqualTo(MovementStatus.RETURNED);
        assertThat(m.getActualReturn()).isEqualTo(returnTime);
    }

    @Test
    void markReturnedWhenScheduledThrows() {
        Movement m = movement(MovementStatus.SCHEDULED);
        when(movementRepository.findById(1L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> movementService.markReturned(1L, null))
                .isInstanceOf(BusinessRuleException.class);

        verify(movementRepository, never()).save(any());
    }

    @Test
    void cancelFromScheduledSucceeds() {
        Movement m = movement(MovementStatus.SCHEDULED);
        when(movementRepository.findById(1L)).thenReturn(Optional.of(m));

        movementService.cancel(1L);

        assertThat(m.getStatus()).isEqualTo(MovementStatus.CANCELLED);
    }

    @Test
    void getOverdueDelegatesToRepositoryQuery() {
        Movement overdue = movement(MovementStatus.OUT);
        overdue.setExpectedReturn(LocalDateTime.now().minusHours(2));
        when(movementRepository.findOverdue(any(LocalDateTime.class))).thenReturn(List.of(overdue));

        assertThat(movementService.getOverdue()).hasSize(1);
        verify(movementRepository).findOverdue(any(LocalDateTime.class));
    }
}
