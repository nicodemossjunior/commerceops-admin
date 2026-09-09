package com.commerceops.admin.customers.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerOrderSummaryResponse(
        UUID publicId,
        String orderNumber,
        String status,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
