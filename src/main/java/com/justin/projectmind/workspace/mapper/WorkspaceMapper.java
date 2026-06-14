package com.justin.projectmind.workspace.mapper;

import com.justin.projectmind.workspace.dto.WorkspaceResponse;
import com.justin.projectmind.workspace.entity.Workspace;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper
public interface WorkspaceMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    WorkspaceResponse toResponse(Workspace workspace);
}
