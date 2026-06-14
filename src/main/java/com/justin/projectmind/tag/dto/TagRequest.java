package com.justin.projectmind.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "color must be a hex code like #1A2B3C")
        String color
) {
}
