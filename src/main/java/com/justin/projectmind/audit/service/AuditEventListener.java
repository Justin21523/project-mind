package com.justin.projectmind.audit.service;

import com.justin.projectmind.audit.AuditEvent;
import com.justin.projectmind.audit.entity.AuditLog;
import com.justin.projectmind.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Persists {@link AuditEvent}s after the originating transaction commits. Running in a
 * new transaction (REQUIRES_NEW) ensures a failed audit write can never roll back the
 * business operation that already succeeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUserId(event.userId());
            entry.setAction(event.action());
            entry.setEntityType(event.entityType());
            entry.setEntityId(event.entityId());
            entry.setDetails(event.details());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist audit event {}: {}", event, ex.getMessage());
        }
    }
}
