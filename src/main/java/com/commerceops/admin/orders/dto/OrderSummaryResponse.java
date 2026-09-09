package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID publicId,
        String orderNumber,
        UUID customerPublicId,
        String customerName,
        OrderStatus status,
        PaymentStatus paymentStatus,
        DeliveryStatus deliveryStatus,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
