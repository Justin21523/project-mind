package com.justin.projectmind.task.mapper;

import com.justin.projectmind.task.dto.TaskResponse;
import com.justin.projectmind.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TaskMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "ownerId", source = "owner.id")
    TaskResponse toResponse(Task task);
}
