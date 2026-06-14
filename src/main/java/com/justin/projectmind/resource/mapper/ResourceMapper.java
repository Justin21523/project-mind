package com.justin.projectmind.resource.mapper;

import com.justin.projectmind.resource.dto.ResourceResponse;
import com.justin.projectmind.resource.entity.Resource;
import com.justin.projectmind.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TagMapper.class)
public interface ResourceMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "ownerId", source = "owner.id")
    ResourceResponse toResponse(Resource resource);
}
