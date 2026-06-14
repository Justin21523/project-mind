package com.justin.projectmind.modelregistry.mapper;

import com.justin.projectmind.modelregistry.dto.AiModelResponse;
import com.justin.projectmind.modelregistry.entity.AiModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AiModelMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    AiModelResponse toResponse(AiModel model);
}
