package com.commerceops.admin.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderCancelRequest(
        @NotBlank(message = "Cancellation reason is required.")
        @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters.")
        String reason
) {
}
