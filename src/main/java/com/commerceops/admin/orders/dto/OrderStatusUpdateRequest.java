package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required.")
        OrderStatus status,
        @Size(max = 1000, message = "Status change reason must not exceed 1000 characters.")
        String reason
) {
}
