package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.common.enums.ReleaseStatus;
import com.kt.cdp.dto.release.ReleaseMapper;
import com.kt.cdp.dto.release.ReleaseStatusUpdateRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.ReleaseRecord;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.ReleaseRecordRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the release state machine in {@link ReleaseService}. */
@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

    @Mock private ReleaseRecordRepository releaseRecordRepository;
    @Mock private PrisonerRepository prisonerRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReleaseMapper releaseMapper;
    @Mock private AuditService auditService;

    @InjectMocks private ReleaseService releaseService;

    private static final long CURRENT_USER_ID = 100L;

    @BeforeEach
    void setUp() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername("officer");
        currentUser.setPasswordHash("x");
        currentUser.setEmail("o@cdp.local");
        currentUser.setFullName("Officer");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(currentUser), null));

        lenient().when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(currentUser));
        lenient().when(releaseRecordRepository.save(any(ReleaseRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ReleaseRecord record(ReleaseStatus status) {
        ReleaseRecord r = new ReleaseRecord();
        r.setId(1L);
        r.setStatus(status);
        return r;
    }

    @Test
    void validTransitionUpcomingToReady() {
        ReleaseRecord entity = record(ReleaseStatus.UPCOMING);
        when(releaseRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        releaseService.updateStatus(1L, new ReleaseStatusUpdateRequest(ReleaseStatus.READY, null, null));

        assertThat(entity.getStatus()).isEqualTo(ReleaseStatus.READY);
        assertThat(entity.getAuthorizedBy()).isNotNull();
    }

    @Test
    void releasingSetsActualDateAndFlipsPrisonerStatus() {
        ReleaseRecord entity = record(ReleaseStatus.READY);
        Prisoner prisoner = new Prisoner();
        prisoner.setStatus(PrisonerStatus.ACTIVE);
        entity.setPrisoner(prisoner);
        when(releaseRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        releaseService.updateStatus(1L, new ReleaseStatusUpdateRequest(ReleaseStatus.RELEASED, null, null));

        assertThat(entity.getStatus()).isEqualTo(ReleaseStatus.RELEASED);
        assertThat(entity.getActualReleaseDate()).isNotNull();
        assertThat(prisoner.getStatus()).isEqualTo(PrisonerStatus.RELEASED);
        verify(prisonerRepository).save(prisoner);
    }

    @Test
    void invalidTransitionReleasedToUpcomingThrows() {
        ReleaseRecord entity = record(ReleaseStatus.RELEASED);
        when(releaseRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                releaseService.updateStatus(1L, new ReleaseStatusUpdateRequest(ReleaseStatus.UPCOMING, null, null)))
                .isInstanceOf(BusinessRuleException.class);

        verify(releaseRecordRepository, never()).save(any());
    }

    @Test
    void cancelledIsTerminal() {
        ReleaseRecord entity = record(ReleaseStatus.CANCELLED);
        when(releaseRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                releaseService.updateStatus(1L, new ReleaseStatusUpdateRequest(ReleaseStatus.READY, null, null)))
                .isInstanceOf(BusinessRuleException.class);
    }
}
