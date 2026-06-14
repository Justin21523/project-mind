package com.justin.projectmind.resource.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.resource.dto.ResourceRequest;
import com.justin.projectmind.resource.dto.ResourceResponse;
import com.justin.projectmind.resource.entity.ResourceType;
import com.justin.projectmind.resource.service.ResourceService;
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
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Resources", description = "Manage technical resource bookmarks")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a resource bookmark")
    public ApiResponse<ResourceResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                                @Valid @RequestBody ResourceRequest request) {
        return ApiResponse.success("Resource created", resourceService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List resources (paginated; filter by workspace, type, title)")
    public ApiResponse<PageResponse<ResourceResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(resourceService.list(principal.getId(), workspaceId, type, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a resource by id")
    public ApiResponse<ResourceResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                                 @PathVariable Long id) {
        return ApiResponse.success(resourceService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resource")
    public ApiResponse<ResourceResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                                @PathVariable Long id,
                                                @Valid @RequestBody ResourceRequest request) {
        return ApiResponse.success("Resource updated", resourceService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a resource (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        resourceService.delete(principal.getId(), id);
    }
}
