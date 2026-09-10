package com.commerceops.admin.dashboard.dto;

import com.commerceops.admin.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderSummaryResponse(
        UUID publicId,
        String orderNumber,
        UUID customerPublicId,
        String customerName,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
