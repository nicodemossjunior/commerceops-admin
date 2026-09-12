package com.commerceops.admin.audit.dto;

import com.commerceops.admin.audit.model.AuditAction;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record AuditLogDetailResponse(
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
        @Schema(
                description = "Optional contextual metadata. Passwords, password hashes, JWTs, tokens, secrets, "
                        + "authorization values, credentials, and API keys are replaced with [REDACTED] before persistence."
        )
        JsonNode metadata,
        Instant createdAt
) {
}
