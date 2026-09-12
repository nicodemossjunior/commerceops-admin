package com.commerceops.admin.audit.service;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.model.AuditLog;
import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRecorder.class);

    private final AuditWriteService auditWriteService;
    private final AuditMetadataRedactor metadataRedactor;
    private final AuditRequestContext requestContext;
    private final CurrentUserProvider currentUserProvider;

    public AuditRecorder(
            AuditWriteService auditWriteService,
            AuditMetadataRedactor metadataRedactor,
            AuditRequestContext requestContext,
            CurrentUserProvider currentUserProvider
    ) {
        this.auditWriteService = auditWriteService;
        this.metadataRedactor = metadataRedactor;
        this.requestContext = requestContext;
        this.currentUserProvider = currentUserProvider;
    }

    public void record(
            AuditAction action,
            String entityType,
            UUID entityPublicId,
            Map<String, ?> metadata
    ) {
        try {
            CurrentUser actor = currentUserProvider.currentUser();
            recordInternal(actor.id(), actor.email(), action, entityType, entityPublicId, metadata);
        } catch (RuntimeException exception) {
            logFailure(action, exception);
        }
    }

    public void recordAs(
            Long actorUserId,
            String actorEmail,
            AuditAction action,
            String entityType,
            UUID entityPublicId,
            Map<String, ?> metadata
    ) {
        try {
            recordInternal(actorUserId, actorEmail, action, entityType, entityPublicId, metadata);
        } catch (RuntimeException exception) {
            logFailure(action, exception);
        }
    }

    private void recordInternal(
            Long actorUserId,
            String actorEmail,
            AuditAction action,
            String entityType,
            UUID entityPublicId,
            Map<String, ?> metadata
    ) {
        AuditRequestContext.RequestDetails request = requestContext.current();
        auditWriteService.write(new AuditLog(
                actorUserId,
                normalizeEmail(actorEmail),
                action,
                entityType,
                entityPublicId,
                request.traceId(),
                request.method(),
                request.path(),
                metadataRedactor.redact(metadata)
        ));
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private void logFailure(AuditAction action, RuntimeException exception) {
        LOGGER.error("Failed to persist audit action {}.", action, exception);
    }
}
