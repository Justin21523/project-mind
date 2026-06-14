package com.justin.projectmind.user.dto;

import com.justin.projectmind.user.entity.Role;

import java.time.Instant;
import java.util.Set;

/**
 * Public representation of a user. Deliberately omits the password hash.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        Set<Role> roles,
        Instant createdAt
) {
}
