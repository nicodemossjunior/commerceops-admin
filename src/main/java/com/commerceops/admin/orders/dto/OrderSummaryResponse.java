package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrderSummaryResponse(
        UUID publicId,
        String orderNumber,
        UUID customerPublicId,
        String customerName,
        @Schema(allowableValues = {"PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        OrderStatus status,
        @Schema(allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"}) PaymentStatus paymentStatus,
        @Schema(allowableValues = {"PENDING", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED"})
        DeliveryStatus deliveryStatus,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
