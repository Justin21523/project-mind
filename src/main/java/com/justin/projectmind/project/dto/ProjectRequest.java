package com.justin.projectmind.project.dto;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.project.entity.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record ProjectRequest(

        @NotNull
        Long workspaceId,

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 4000)
        String description,

        @NotNull
        ProjectStatus status,

        @NotNull
        Priority priority,

        @Size(max = 500)
        String repositoryUrl,

        LocalDate startDate,

        LocalDate targetDate,

        @Min(0)
        @Max(100)
        int progress,

        Set<@Size(max = 60) String> techStack,

        Set<Long> tagIds
) {
}
