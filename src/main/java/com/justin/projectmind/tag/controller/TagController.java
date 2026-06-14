package com.justin.projectmind.tag.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.tag.dto.TagRequest;
import com.justin.projectmind.tag.dto.TagResponse;
import com.justin.projectmind.tag.service.TagService;
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
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tags", description = "Manage reusable tags")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a tag")
    public ApiResponse<TagResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @Valid @RequestBody TagRequest request) {
        return ApiResponse.success("Tag created", tagService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List tags (paginated, optional name search)")
    public ApiResponse<PageResponse<TagResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.success(tagService.list(principal.getId(), search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tag by id")
    public ApiResponse<TagResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @PathVariable Long id) {
        return ApiResponse.success(tagService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tag")
    public ApiResponse<TagResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                           @PathVariable Long id,
                                           @Valid @RequestBody TagRequest request) {
        return ApiResponse.success("Tag updated", tagService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a tag (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        tagService.delete(principal.getId(), id);
    }
}
