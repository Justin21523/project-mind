package com.justin.projectmind.tag.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * Implements {@link Serializable} because instances may be stored in the Redis cache.
 */
public record TagResponse(
        Long id,
        String name,
        String color,
        Long ownerId,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
