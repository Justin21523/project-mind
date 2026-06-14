package com.justin.projectmind.prompt.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.prompt.dto.PromptRequest;
import com.justin.projectmind.prompt.dto.PromptResponse;
import com.justin.projectmind.prompt.service.PromptService;
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
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Prompts", description = "Manage reusable AI prompts")
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a prompt")
    public ApiResponse<PromptResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                              @Valid @RequestBody PromptRequest request) {
        return ApiResponse.success("Prompt created", promptService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List prompts (paginated; filter by workspace, target model, title)")
    public ApiResponse<PageResponse<PromptResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) String targetModel,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {
        return ApiResponse.success(
                promptService.list(principal.getId(), workspaceId, targetModel, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a prompt by id")
    public ApiResponse<PromptResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                               @PathVariable Long id) {
        return ApiResponse.success(promptService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a prompt (bumps its editorial version)")
    public ApiResponse<PromptResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                              @PathVariable Long id,
                                              @Valid @RequestBody PromptRequest request) {
        return ApiResponse.success("Prompt updated", promptService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a prompt (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        promptService.delete(principal.getId(), id);
    }
}
