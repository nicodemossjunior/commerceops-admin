package com.commerceops.admin.audit.dto;

import com.commerceops.admin.audit.model.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record AuditLogSummaryResponse(
        UUID publicId,
        @Schema(description = "Actor public UUID; null when no user could be identified")
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
