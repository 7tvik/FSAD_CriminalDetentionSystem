package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.dto.auth.AuthResponse;
import com.kt.cdp.dto.auth.ChangePasswordRequest;
import com.kt.cdp.dto.auth.RegisterRequest;
import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.repository.RoleRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.CustomUserDetails;
import com.kt.cdp.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link AuthService}: registration policy and password change. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private AuditService auditService;

    @InjectMocks private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerDuplicateUsernameThrowsConflict() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("taken", "a@cdp.local", "Passw0rd", "Full Name", null);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerAssignsDefaultReadOnlyRoleIgnoringRequestedRoles() {
        Role govt = new Role();
        govt.setId(4L);
        govt.setName("GOVERNMENT_OFFICIAL");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@cdp.local")).thenReturn(false);
        when(roleRepository.findByName("GOVERNMENT_OFFICIAL")).thenReturn(Optional.of(govt));
        when(passwordEncoder.encode("Passw0rd")).thenReturn("hashed");
        when(jwtService.generateToken("newuser")).thenReturn("jwt-token");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Client attempts to self-assign ADMINISTRATOR; it must be ignored.
        RegisterRequest req = new RegisterRequest("newuser", "new@cdp.local", "Passw0rd",
                "New User", Set.of("ADMINISTRATOR"));

        AuthResponse response = authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRoles()).containsExactly(govt);
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.roles()).containsExactly("GOVERNMENT_OFFICIAL");
    }

    @Test
    void changePasswordWithWrongCurrentThrows() {
        User user = new User();
        user.setId(100L);
        user.setUsername("me");
        user.setPasswordHash("stored-hash");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null));

        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.changePassword(new ChangePasswordRequest("wrong-current", "NewPassw0rd")))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
