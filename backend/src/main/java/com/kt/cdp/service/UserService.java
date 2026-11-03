package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.user.CreateUserRequest;
import com.kt.cdp.dto.user.UpdateUserRequest;
import com.kt.cdp.dto.user.UserDto;
import com.kt.cdp.dto.user.UserMapper;
import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ConflictException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.RoleRepository;
import com.kt.cdp.repository.UserRepository;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Admin user management: creation, role assignment and activation state. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, UserMapper userMapper,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRoles(resolveRoles(request.roles()));
        userRepository.save(user);

        auditService.record("CREATE", "User", user.getId(), null, describe(user));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));

        String before = describe(user);
        boolean rolesChanged = false;

        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<String> oldRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            user.setRoles(resolveRoles(request.roles()));
            rolesChanged = !oldRoleNames.equals(request.roles());
        }
        userRepository.save(user);

        auditService.record("UPDATE", "User", user.getId(), before, describe(user));
        if (rolesChanged) {
            auditService.record("ROLE_CHANGE", "User", user.getId(), before, describe(user));
        }
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        user.setActive(true);
        userRepository.save(user);
        auditService.record("ACTIVATE", "User", user.getId());
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto deactivate(Long id) {
        Long currentUserId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        if (currentUserId.equals(id)) {
            throw new BusinessRuleException("You cannot deactivate your own account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        user.setActive(false);
        userRepository.save(user);
        auditService.record("DEACTIVATE", "User", user.getId());
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentProfile() {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessRuleException("No authenticated user"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return userMapper.toDto(user);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new LinkedHashSet<>();
        for (String name : roleNames) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> ResourceNotFoundException.of("Role", name));
            roles.add(role);
        }
        return roles;
    }

    private String describe(User user) {
        String roles = user.getRoles().stream().map(Role::getName).sorted().collect(Collectors.joining(","));
        return "email=" + user.getEmail() + "; fullName=" + user.getFullName()
                + "; active=" + user.isActive() + "; roles=[" + roles + "]";
    }
}
