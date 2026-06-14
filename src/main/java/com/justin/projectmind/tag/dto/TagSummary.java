package com.justin.projectmind.tag.dto;

import java.io.Serializable;

/**
 * Compact tag projection embedded in other resources' responses.
 */
public record TagSummary(
        Long id,
        String name,
        String color
) implements Serializable {
}
