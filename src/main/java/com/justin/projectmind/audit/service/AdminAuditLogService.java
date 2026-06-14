package com.justin.projectmind.audit.service;

import com.justin.projectmind.audit.dto.AuditLogResponse;
import com.justin.projectmind.audit.entity.AuditLog;
import com.justin.projectmind.audit.mapper.AuditLogMapper;
import com.justin.projectmind.audit.repository.AuditLogRepository;
import com.justin.projectmind.common.pagination.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * System-wide, cross-user audit log access for administrators. Unlike
 * {@link AuditLogQueryService} (which is scoped to the calling user), this can read every
 * user's trail, optionally filtered by user and/or entity type.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public PageResponse<AuditLogResponse> list(Long userId, String entityType, Pageable pageable) {
        boolean hasType = StringUtils.hasText(entityType);
        Page<AuditLog> page;
        if (userId != null && hasType) {
            page = auditLogRepository.findByUserIdAndEntityType(userId, entityType, pageable);
        } else if (userId != null) {
            page = auditLogRepository.findByUserId(userId, pageable);
        } else if (hasType) {
            page = auditLogRepository.findByEntityType(entityType, pageable);
        } else {
            page = auditLogRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(auditLogMapper::toResponse));
    }
}
