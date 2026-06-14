package com.justin.projectmind.workspace.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.workspace.dto.WorkspaceRequest;
import com.justin.projectmind.workspace.dto.WorkspaceResponse;
import com.justin.projectmind.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workspaces", description = "Manage workspaces that group projects, notes and resources")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a workspace")
    public ApiResponse<WorkspaceResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                                  @Valid @RequestBody WorkspaceRequest request) {
        return ApiResponse.success("Workspace created", workspaceService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List workspaces (paginated, optional name search)")
    public ApiResponse<PageResponse<WorkspaceResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(workspaceService.list(principal.getId(), search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a workspace by id")
    public ApiResponse<WorkspaceResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                                   @PathVariable Long id) {
        return ApiResponse.success(workspaceService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a workspace")
    public ApiResponse<WorkspaceResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody WorkspaceRequest request) {
        return ApiResponse.success("Workspace updated", workspaceService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a workspace (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        workspaceService.delete(principal.getId(), id);
    }
}
