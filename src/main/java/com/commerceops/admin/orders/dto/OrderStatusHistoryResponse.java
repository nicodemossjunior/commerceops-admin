package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID publicId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String reason,
        Instant createdAt
) {
}
