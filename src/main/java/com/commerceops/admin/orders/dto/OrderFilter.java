package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderFilter(
        String orderNumber,
        UUID customerId,
        OrderStatus status,
        PaymentStatus paymentStatus,
        DeliveryStatus deliveryStatus,
        Instant createdFrom,
        Instant createdTo
) {
}
