package com.commerceops.admin.audit.dto;

import com.commerceops.admin.audit.model.AuditAction;
import java.time.Instant;
import java.util.UUID;

public record AuditLogSummaryResponse(
        UUID publicId,
        UUID actorUserId,
        String actorEmail,
        AuditAction action,
        String entityType,
        UUID entityPublicId,
        String traceId,
        String requestMethod,
        String requestPath,
        Instant createdAt
) {
}
