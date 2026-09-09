package com.commerceops.admin.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID publicId,
        UUID productPublicId,
        String productSku,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalAmount
) {
}
