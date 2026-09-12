package com.commerceops.admin.audit.dto;

import com.commerceops.admin.audit.model.AuditAction;
import java.time.Instant;
import java.util.UUID;

public record AuditLogFilter(
        UUID actorUserId,
        String actorEmail,
        AuditAction action,
        String entityType,
        UUID entityPublicId,
        Instant createdFrom,
        Instant createdTo
) {
}
