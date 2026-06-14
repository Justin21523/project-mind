package com.justin.projectmind.audit.dto;

import com.justin.projectmind.audit.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long userId,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        Instant createdAt
) {
}
