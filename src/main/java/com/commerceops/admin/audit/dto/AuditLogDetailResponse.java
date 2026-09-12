package com.commerceops.admin.audit.dto;

import com.commerceops.admin.audit.model.AuditAction;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record AuditLogDetailResponse(
        UUID publicId,
        UUID actorUserId,
        String actorEmail,
        AuditAction action,
        String entityType,
        UUID entityPublicId,
        String traceId,
        String requestMethod,
        String requestPath,
        JsonNode metadata,
        Instant createdAt
) {
}
