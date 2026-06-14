package com.justin.projectmind.project.dto;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.project.entity.ProjectStatus;
import com.justin.projectmind.tag.dto.TagSummary;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record ProjectResponse(
        Long id,
        Long workspaceId,
        Long ownerId,
        String name,
        String description,
        ProjectStatus status,
        Priority priority,
        String repositoryUrl,
        LocalDate startDate,
        LocalDate targetDate,
        int progress,
        Set<String> techStack,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
