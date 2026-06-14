package com.justin.projectmind.audit;

/**
 * Published by feature services after a mutating operation. A listener persists it to
 * the audit log only once the surrounding transaction commits, keeping auditing
 * decoupled from business logic.
 *
 * @param userId     the acting user
 * @param action     create / update / delete
 * @param entityType the affected entity type, e.g. "PROJECT"
 * @param entityId   the affected entity id (may be null for bulk actions)
 * @param details    optional human-readable detail
 */
public record AuditEvent(
        Long userId,
        AuditAction action,
        String entityType,
        Long entityId,
        String details
) {

    public static AuditEvent of(Long userId, AuditAction action, String entityType, Long entityId) {
        return new AuditEvent(userId, action, entityType, entityId, null);
    }
}
