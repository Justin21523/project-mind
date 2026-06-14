package com.justin.projectmind.task.dto;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(

        @NotNull
        Long workspaceId,

        Long projectId,

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 4000)
        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        Priority priority,

        LocalDate dueDate
) {
}
