package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID publicId,
        String orderNumber,
        UUID customerPublicId,
        String customerName,
        OrderStatus status,
        PaymentStatus paymentStatus,
        DeliveryStatus deliveryStatus,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal shippingAmount,
        BigDecimal totalAmount,
        Instant cancelledAt,
        Instant refundedAt,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory
) {
}
