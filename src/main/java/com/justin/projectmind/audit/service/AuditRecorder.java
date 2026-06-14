package com.justin.projectmind.audit.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin facade feature services use to emit audit events without depending on the
 * publishing mechanism directly.
 */
@Component
@RequiredArgsConstructor
public class AuditRecorder {

    private final ApplicationEventPublisher publisher;

    public void record(Long userId, AuditAction action, String entityType, Long entityId) {
        publisher.publishEvent(AuditEvent.of(userId, action, entityType, entityId));
    }
}
