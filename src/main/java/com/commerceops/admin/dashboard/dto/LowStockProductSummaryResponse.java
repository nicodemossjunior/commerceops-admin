package com.commerceops.admin.dashboard.dto;

import com.commerceops.admin.catalog.model.ProductStatus;
import java.util.UUID;

public record LowStockProductSummaryResponse(
        UUID publicId,
        String sku,
        String name,
        int stockQuantity,
        ProductStatus status
) {
}
