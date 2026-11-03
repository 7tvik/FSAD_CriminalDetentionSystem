package com.kt.cdp.dto.user;

import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/** Maps User entities to their API representation. Never exposes the password hash. */
@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
