package com.commerceops.admin.audit.service;

import com.commerceops.admin.audit.model.AuditLog;
import com.commerceops.admin.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditWriteService {

    private final AuditLogRepository auditLogRepository;

    public AuditWriteService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditLog auditLog) {
        auditLogRepository.saveAndFlush(auditLog);
    }
}
