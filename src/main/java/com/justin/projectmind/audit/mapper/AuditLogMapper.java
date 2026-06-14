package com.justin.projectmind.audit.mapper;

import com.justin.projectmind.audit.dto.AuditLogResponse;
import com.justin.projectmind.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
