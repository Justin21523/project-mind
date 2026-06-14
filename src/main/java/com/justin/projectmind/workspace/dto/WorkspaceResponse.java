package com.justin.projectmind.workspace.dto;

import java.time.Instant;

public record WorkspaceResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        Instant createdAt,
        Instant updatedAt
) {
}
