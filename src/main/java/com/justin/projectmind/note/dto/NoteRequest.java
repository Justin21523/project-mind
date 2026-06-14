package com.justin.projectmind.note.dto;

import com.justin.projectmind.note.entity.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record NoteRequest(

        @NotNull
        Long workspaceId,

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        @NotNull
        NoteType type,

        Set<Long> tagIds
) {
}
