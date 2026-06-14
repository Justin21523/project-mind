package com.justin.projectmind.user.controller;

import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.user.dto.UserResponse;
import com.justin.projectmind.user.mapper.UserMapper;
import com.justin.projectmind.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal SecurityUserDetails principal) {
        UserResponse response = userMapper.toResponse(userService.getById(principal.getId()));
        return ApiResponse.success(response);
    }
}
