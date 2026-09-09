package com.commerceops.admin.customers.dto;

import com.commerceops.admin.customers.model.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID publicId,
        String name,
        String email,
        String phone,
        String document,
        @Schema(allowableValues = {"ACTIVE", "INACTIVE", "BLOCKED"}) CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
