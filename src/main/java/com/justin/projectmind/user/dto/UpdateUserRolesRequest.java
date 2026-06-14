package com.justin.projectmind.user.dto;

import com.justin.projectmind.user.entity.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(

        @NotEmpty
        Set<Role> roles
) {
}
