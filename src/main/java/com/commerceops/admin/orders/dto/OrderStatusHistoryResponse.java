package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.OrderStatus;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrderStatusHistoryResponse(
        UUID publicId,
        @Schema(allowableValues = {"PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        OrderStatus fromStatus,
        @Schema(allowableValues = {"PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        OrderStatus toStatus,
        String reason,
        Instant createdAt
) {
}
