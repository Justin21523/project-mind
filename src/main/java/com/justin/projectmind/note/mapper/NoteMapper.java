package com.justin.projectmind.note.mapper;

import com.justin.projectmind.note.dto.NoteResponse;
import com.justin.projectmind.note.entity.Note;
import com.justin.projectmind.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TagMapper.class)
public interface NoteMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "ownerId", source = "owner.id")
    NoteResponse toResponse(Note note);
}
