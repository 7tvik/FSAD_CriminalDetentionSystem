package com.kt.cdp.service;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.dto.audit.AuditLogResponse;
import com.kt.cdp.entity.AuditLog;
import com.kt.cdp.repository.AuditLogRepository;
import com.kt.cdp.specification.AuditLogSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * READ-ONLY access to the append-only audit trail. Audit records are written
 * exclusively by {@link com.kt.cdp.audit.AuditService}; no mutation is exposed here.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String entityType, Long userId, String action,
                                                 LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AuditLog> spec = Specification.allOf(
                AuditLogSpecifications.entityType(entityType),
                AuditLogSpecifications.userId(userId),
                AuditLogSpecifications.action(action),
                AuditLogSpecifications.createdFrom(from),
                AuditLogSpecifications.createdTo(to));
        return PageResponse.of(auditLogRepository.findAll(spec, pageable).map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUsernameSnapshot(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress(),
                log.getCreatedAt());
    }
}
