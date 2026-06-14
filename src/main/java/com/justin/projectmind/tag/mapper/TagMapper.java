package com.justin.projectmind.tag.mapper;

import com.justin.projectmind.tag.dto.TagResponse;
import com.justin.projectmind.tag.dto.TagSummary;
import com.justin.projectmind.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TagMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    TagResponse toResponse(Tag tag);

    TagSummary toSummary(Tag tag);
}
