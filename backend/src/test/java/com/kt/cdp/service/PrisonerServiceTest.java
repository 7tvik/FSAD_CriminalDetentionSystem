package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.dto.prisoner.PrisonerMapper;
import com.kt.cdp.dto.prisoner.PrisonerRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.CaseRepository;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import com.kt.cdp.repository.SurrenderedArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for prisoner creation rules and soft delete. */
@ExtendWith(MockitoExtension.class)
class PrisonerServiceTest {

    @Mock private PrisonerRepository prisonerRepository;
    @Mock private CaseRepository caseRepository;
    @Mock private SurrenderedArticleRepository articleRepository;
    @Mock private ReleaseRecordRepository releaseRepository;
    @Mock private MovementRepository movementRepository;
    @Mock private PrisonerMapper prisonerMapper;
    @Mock private AuditService auditService;

    @InjectMocks private PrisonerService prisonerService;

    private PrisonerRequest request(LocalDate admission, LocalDate expectedRelease) {
        return new PrisonerRequest("P-001", "John", "Doe", LocalDate.of(1990, 1, 1),
                "M", "addr", "contact", "PASSPORT", "X1",
                admission, PrisonerStatus.ACTIVE, "Facility", "W1", "C1", expectedRelease, null);
    }

    @Test
    void duplicatePrisonerNumberThrowsConflict() {
        when(prisonerRepository.existsByPrisonerNumber("P-001")).thenReturn(true);

        assertThatThrownBy(() -> prisonerService.create(request(LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(ConflictException.class);

        verify(prisonerRepository, never()).save(any());
    }

    @Test
    void expectedReleaseBeforeAdmissionThrows() {
        when(prisonerRepository.existsByPrisonerNumber("P-001")).thenReturn(false);
        PrisonerRequest req = request(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> prisonerService.create(req))
                .isInstanceOf(BusinessRuleException.class);

        verify(prisonerRepository, never()).save(any());
    }

    @Test
    void softDeleteDeactivatesAndMarksInactive() {
        Prisoner prisoner = new Prisoner();
        prisoner.setId(5L);
        prisoner.setActive(true);
        prisoner.setStatus(PrisonerStatus.ACTIVE);
        when(prisonerRepository.findById(5L)).thenReturn(Optional.of(prisoner));

        prisonerService.softDelete(5L);

        assertThat(prisoner.isActive()).isFalse();
        assertThat(prisoner.getStatus()).isEqualTo(PrisonerStatus.INACTIVE);
        verify(prisonerRepository).save(prisoner);
    }

    @Test
    void getByIdMissingThrowsNotFound() {
        when(prisonerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prisonerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
