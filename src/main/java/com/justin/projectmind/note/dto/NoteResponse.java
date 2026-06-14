package com.justin.projectmind.note.dto;

import com.justin.projectmind.note.entity.NoteType;
import com.justin.projectmind.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record NoteResponse(
        Long id,
        Long workspaceId,
        Long ownerId,
        String title,
        String content,
        NoteType type,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
