package com.justin.projectmind.task.dto;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.task.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        Long workspaceId,
        Long projectId,
        Long ownerId,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
}
