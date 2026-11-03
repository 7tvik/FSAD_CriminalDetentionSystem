package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.dto.auth.AuthResponse;
import com.kt.cdp.dto.auth.ChangePasswordRequest;
import com.kt.cdp.dto.auth.LoginRequest;
import com.kt.cdp.dto.auth.RegisterRequest;
import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.RoleRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.CustomUserDetails;
import com.kt.cdp.security.JwtService;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    /** Role assigned to self-registered accounts (read-only). Admins elevate via user management. */
    private static final String DEFAULT_SELF_REGISTER_ROLE = "GOVERNMENT_OFFICIAL";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    /** Public self-registration. Requested roles are ignored here for security; a read-only role is granted. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_SELF_REGISTER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not configured"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRoles(Set.of(defaultRole));
        userRepository.save(user);

        auditService.record("REGISTER", "User", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = principal.getUser();
        auditService.record("LOGIN", "User", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.record("CHANGE_PASSWORD", "User", user.getId());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUsername());
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(),
                user.getFullName(), roles, permissions);
    }
}
