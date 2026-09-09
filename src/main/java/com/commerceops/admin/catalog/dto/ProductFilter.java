package com.commerceops.admin.catalog.dto;

import com.commerceops.admin.catalog.model.ProductStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilter(
        UUID categoryId,
        ProductStatus status,
        String sku,
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean lowStock
) {
}
