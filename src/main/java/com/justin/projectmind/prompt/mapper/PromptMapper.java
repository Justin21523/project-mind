package com.justin.projectmind.prompt.mapper;

import com.justin.projectmind.prompt.dto.PromptResponse;
import com.justin.projectmind.prompt.entity.Prompt;
import com.justin.projectmind.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TagMapper.class)
public interface PromptMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "ownerId", source = "owner.id")
    PromptResponse toResponse(Prompt prompt);
}
