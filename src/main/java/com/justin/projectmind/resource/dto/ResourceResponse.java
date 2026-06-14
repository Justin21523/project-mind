package com.justin.projectmind.resource.dto;

import com.justin.projectmind.resource.entity.ResourceType;
import com.justin.projectmind.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record ResourceResponse(
        Long id,
        Long workspaceId,
        Long ownerId,
        String title,
        String url,
        ResourceType type,
        String description,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
