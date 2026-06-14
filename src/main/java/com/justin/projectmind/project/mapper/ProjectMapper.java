package com.justin.projectmind.project.mapper;

import com.justin.projectmind.project.dto.ProjectResponse;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TagMapper.class)
public interface ProjectMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "ownerId", source = "owner.id")
    ProjectResponse toResponse(Project project);
}
