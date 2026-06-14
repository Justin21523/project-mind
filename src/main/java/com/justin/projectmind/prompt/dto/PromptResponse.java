package com.justin.projectmind.prompt.dto;

import com.justin.projectmind.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record PromptResponse(
        Long id,
        Long workspaceId,
        Long ownerId,
        String title,
        String content,
        String targetModel,
        String taskType,
        Integer rating,
        int promptVersion,
        String notes,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
