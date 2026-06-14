package com.justin.projectmind.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Static helpers for reading the current principal out of the security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<SecurityUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof SecurityUserDetails details) {
            return Optional.of(details);
        }
        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserDetails().map(SecurityUserDetails::getId);
    }

    public static Optional<String> getCurrentUsername() {
        return getCurrentUserDetails().map(SecurityUserDetails::getUsername);
    }
}
