package com.justin.projectmind.note.controller;

import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.note.dto.NoteRequest;
import com.justin.projectmind.note.dto.NoteResponse;
import com.justin.projectmind.note.entity.NoteType;
import com.justin.projectmind.note.service.NoteService;
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
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notes", description = "Manage Markdown technical notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a note")
    public ApiResponse<NoteResponse> create(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @Valid @RequestBody NoteRequest request) {
        return ApiResponse.success("Note created", noteService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List notes (paginated; filter by workspace, type, title)")
    public ApiResponse<PageResponse<NoteResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) NoteType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {
        return ApiResponse.success(noteService.list(principal.getId(), workspaceId, type, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a note by id")
    public ApiResponse<NoteResponse> getById(@AuthenticationPrincipal SecurityUserDetails principal,
                                             @PathVariable Long id) {
        return ApiResponse.success(noteService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a note")
    public ApiResponse<NoteResponse> update(@AuthenticationPrincipal SecurityUserDetails principal,
                                            @PathVariable Long id,
                                            @Valid @RequestBody NoteRequest request) {
        return ApiResponse.success("Note updated", noteService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a note (soft delete)")
    public void delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long id) {
        noteService.delete(principal.getId(), id);
    }
}
