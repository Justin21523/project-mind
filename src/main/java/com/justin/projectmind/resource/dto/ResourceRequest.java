package com.justin.projectmind.resource.dto;

import com.justin.projectmind.resource.entity.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ResourceRequest(

        @NotNull
        Long workspaceId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 1000)
        String url,

        @NotNull
        ResourceType type,

        @Size(max = 2000)
        String description,

        Set<Long> tagIds
) {
}
