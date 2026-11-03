package com.kt.cdp.audit;

import com.kt.cdp.entity.AuditLog;
import com.kt.cdp.repository.AuditLogRepository;
import com.kt.cdp.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records entries in the append-only audit trail. Writes run in their own
 * transaction so an audit failure never rolls back business operations, and a
 * rolled-back business op does not leave a misleading audit record removed.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, Object entityId, String oldValue, String newValue) {
        AuditLog logEntry = new AuditLog();
        SecurityUtils.currentUser().ifPresent(u -> {
            logEntry.setUserId(u.getId());
            logEntry.setUsernameSnapshot(u.getUsername());
        });
        logEntry.setAction(action);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId == null ? null : String.valueOf(entityId));
        logEntry.setOldValue(oldValue);
        logEntry.setNewValue(newValue);
        auditLogRepository.save(logEntry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, Object entityId) {
        record(action, entityType, entityId, null, null);
    }
}
