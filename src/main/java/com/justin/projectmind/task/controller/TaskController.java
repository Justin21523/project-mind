package com.justin.projectmind.task.controller;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.task.dto.TaskRequest;
import com.justin.projectmind.task.dto.TaskResponse;
import com.justin.projectmind.task.entity.TaskStatus;
import com.justin.projectmind.task.service.TaskService;
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
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "Manage project tasks and roadmap items")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task")
    public ApiResponse<TaskResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @Valid @RequestBody TaskRequest request) {
        return ApiResponse.success("Task created", taskService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List tasks (paginated; filter by workspace, project, status, priority, title)")
    public ApiResponse<PageResponse<TaskResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(
                taskService.list(principal.getId(), workspaceId, projectId, status, priority, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    public ApiResponse<TaskResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                             @PathVariable Long id) {
        return ApiResponse.success(taskService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ApiResponse<TaskResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @PathVariable Long id,
                                            @Valid @RequestBody TaskRequest request) {
        return ApiResponse.success("Task updated", taskService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        taskService.delete(principal.getId(), id);
    }
}
