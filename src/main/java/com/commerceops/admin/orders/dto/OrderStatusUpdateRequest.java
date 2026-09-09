package com.commerceops.admin.orders.dto;

import com.commerceops.admin.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required.")
        @Schema(allowableValues = {"PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        OrderStatus status,
        @Size(max = 1000, message = "Status change reason must not exceed 1000 characters.")
        String reason
) {
}
