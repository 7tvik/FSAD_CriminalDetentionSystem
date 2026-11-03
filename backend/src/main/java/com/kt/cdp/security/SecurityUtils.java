package com.kt.cdp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Convenience accessors for the currently authenticated user. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<CustomUserDetails> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return Optional.of(cud);
        }
        return Optional.empty();
    }

    public static Optional<Long> currentUserId() {
        return currentUser().map(CustomUserDetails::getId);
    }

    public static Optional<String> currentUsername() {
        return currentUser().map(CustomUserDetails::getUsername);
    }
}
