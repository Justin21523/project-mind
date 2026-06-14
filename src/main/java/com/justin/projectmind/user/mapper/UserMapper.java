package com.justin.projectmind.user.mapper;

import com.justin.projectmind.user.dto.UserResponse;
import com.justin.projectmind.user.entity.User;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link User} <-> DTO conversions.
 */
@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
