package com.kt.cdp.service;

import com.kt.cdp.dto.user.RoleDto;
import com.kt.cdp.entity.Permission;
import com.kt.cdp.repository.PermissionRepository;
import com.kt.cdp.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Read-only access to the role/permission catalogue. */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleDto(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listPermissionNames() {
        return permissionRepository.findAll().stream()
                .map(Permission::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
