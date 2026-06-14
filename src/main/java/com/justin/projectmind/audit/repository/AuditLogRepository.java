package com.justin.projectmind.audit.repository;

import com.justin.projectmind.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByUserIdAndEntityType(Long userId, String entityType, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
}
