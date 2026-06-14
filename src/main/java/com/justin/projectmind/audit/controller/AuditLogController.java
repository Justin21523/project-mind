package com.justin.projectmind.audit.controller;

import com.justin.projectmind.audit.dto.AuditLogResponse;
import com.justin.projectmind.audit.service.AuditLogQueryService;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import com.justin.projectmind.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit Logs", description = "Read the current user's action history")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    @Operation(summary = "List audit logs (paginated, optional entityType filter)")
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @AuthenticationPrincipal SecurityUserDetails principal,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(auditLogQueryService.list(principal.getId(), entityType, pageable));
    }
}
