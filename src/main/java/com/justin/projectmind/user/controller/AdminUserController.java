package com.justin.projectmind.user.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.user.dto.UpdateUserRolesRequest;
import com.justin.projectmind.user.dto.UpdateUserStatusRequest;
import com.justin.projectmind.user.dto.UserResponse;
import com.justin.projectmind.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Users", description = "User administration (ADMIN role required)")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all users (paginated, optional username/email search)")
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(adminUserService.listUsers(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any user by id")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getUser(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user")
    public ApiResponse<UserResponse> updateStatus(@AuthenticationPrincipal SecurityUserDetails admin,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        return ApiResponse.success("User status updated",
                adminUserService.updateStatus(admin.getId(), id, request.enabled()));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Replace a user's roles")
    public ApiResponse<UserResponse> updateRoles(@AuthenticationPrincipal SecurityUserDetails admin,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody UpdateUserRolesRequest request) {
        return ApiResponse.success("User roles updated",
                adminUserService.updateRoles(admin.getId(), id, request.roles()));
    }
}
