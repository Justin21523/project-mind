package com.justin.projectmind.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 2000)
        String description
) {
}
