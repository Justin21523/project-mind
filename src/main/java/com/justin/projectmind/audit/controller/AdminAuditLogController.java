package com.justin.projectmind.audit.controller;

import com.justin.projectmind.audit.dto.AuditLogResponse;
import com.justin.projectmind.audit.service.AdminAuditLogService;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Audit Logs", description = "System-wide audit trail (ADMIN role required)")
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    @Operation(summary = "List audit logs across all users (paginated; filter by user and/or entityType)")
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(adminAuditLogService.list(userId, entityType, pageable));
    }
}
