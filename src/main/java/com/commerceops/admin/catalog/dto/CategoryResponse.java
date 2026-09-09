package com.commerceops.admin.catalog.dto;

import com.commerceops.admin.catalog.model.CategoryStatus;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID publicId,
        UUID parentPublicId,
        String name,
        String slug,
        String description,
        CategoryStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
