package com.commerceops.admin.catalog.dto;

import com.commerceops.admin.catalog.model.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductResponse(
        UUID publicId,
        UUID categoryPublicId,
        String sku,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String imageUrl,
        int stockQuantity,
        @Schema(allowableValues = {"DRAFT", "ACTIVE", "INACTIVE", "OUT_OF_STOCK"})
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
