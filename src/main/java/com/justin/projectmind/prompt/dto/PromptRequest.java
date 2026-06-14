package com.justin.projectmind.prompt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PromptRequest(

        @NotNull
        Long workspaceId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String content,

        @Size(max = 100)
        String targetModel,

        @Size(max = 100)
        String taskType,

        @Min(1)
        @Max(5)
        Integer rating,

        @Size(max = 2000)
        String notes,

        Set<Long> tagIds
) {
}
