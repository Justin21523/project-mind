package com.justin.projectmind.project.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.project.dto.ProjectRequest;
import com.justin.projectmind.project.dto.ProjectResponse;
import com.justin.projectmind.project.entity.ProjectStatus;
import com.justin.projectmind.project.service.ProjectService;
import com.justin.projectmind.security.SecurityUserDetails;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Projects", description = "Manage personal side projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project")
    public ApiResponse<ProjectResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                               @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success("Project created", projectService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List projects (paginated; filter by workspace, status, name)")
    public ApiResponse<PageResponse<ProjectResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(projectService.list(principal.getId(), workspaceId, status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by id")
    public ApiResponse<ProjectResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                                @PathVariable Long id) {
        return ApiResponse.success(projectService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ApiResponse<ProjectResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success("Project updated", projectService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a project (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        projectService.delete(principal.getId(), id);
    }
}
