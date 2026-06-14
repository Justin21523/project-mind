package com.justin.projectmind.audit.service;

import com.justin.projectmind.audit.dto.AuditLogResponse;
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
 * Read-only access to a user's own audit trail.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public PageResponse<AuditLogResponse> list(Long userId, String entityType, Pageable pageable) {
        Page<AuditLogResponse> page = (StringUtils.hasText(entityType)
                ? auditLogRepository.findByUserIdAndEntityType(userId, entityType, pageable)
                : auditLogRepository.findByUserId(userId, pageable))
                .map(auditLogMapper::toResponse);
        return PageResponse.from(page);
    }
}
