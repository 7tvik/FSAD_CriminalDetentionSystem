package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.InterviewStatus;
import com.kt.cdp.dto.interview.InterviewMapper;
import com.kt.cdp.dto.interview.InterviewRequestDto;
import com.kt.cdp.dto.interview.InterviewReviewRequest;
import com.kt.cdp.entity.InterviewRequest;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.repository.InterviewRequestRepository;
import com.kt.cdp.repository.PrisonerRepository;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the interview-request state machine and duplicate detection. */
@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock private InterviewRequestRepository interviewRequestRepository;
    @Mock private PrisonerRepository prisonerRepository;
    @Mock private UserRepository userRepository;
    @Mock private InterviewMapper interviewMapper;
    @Mock private AuditService auditService;

    @InjectMocks private InterviewService interviewService;

    private static final long CURRENT_USER_ID = 100L;

    @BeforeEach
    void setUp() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername("reviewer");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(currentUser), null));
        lenient().when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(currentUser));
        lenient().when(interviewRequestRepository.save(any(InterviewRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private InterviewRequest request(InterviewStatus status) {
        InterviewRequest r = new InterviewRequest();
        r.setId(1L);
        r.setStatus(status);
        return r;
    }

    @Test
    void approveFromPendingSetsReviewerAndDate() {
        InterviewRequest entity = request(InterviewStatus.PENDING);
        when(interviewRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        interviewService.approve(1L, new InterviewReviewRequest("looks good"));

        assertThat(entity.getStatus()).isEqualTo(InterviewStatus.APPROVED);
        assertThat(entity.getReviewedBy()).isNotNull();
        assertThat(entity.getReviewDate()).isNotNull();
    }

    @Test
    void invalidPendingToCompletedThrows() {
        InterviewRequest entity = request(InterviewStatus.PENDING);
        when(interviewRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> interviewService.complete(1L, new InterviewReviewRequest(null)))
                .isInstanceOf(BusinessRuleException.class);

        verify(interviewRequestRepository, never()).save(any());
    }

    @Test
    void duplicateCreateThrowsConflict() {
        Prisoner prisoner = new Prisoner();
        prisoner.setId(7L);
        InterviewRequestDto dto = new InterviewRequestDto(
                7L, "Jane Visitor", "sister", "555", "ID-1",
                LocalDate.of(2026, 1, 10), null, "visit");

        when(prisonerRepository.findById(7L)).thenReturn(Optional.of(prisoner));
        when(interviewRequestRepository
                .existsByPrisonerIdAndVisitorNameIgnoreCaseAndRequestedDateAndStatusIn(
                        eq(7L), anyString(), any(LocalDate.class), any(Set.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> interviewService.create(dto))
                .isInstanceOf(ConflictException.class);

        verify(interviewRequestRepository, never()).save(any());
    }
}
