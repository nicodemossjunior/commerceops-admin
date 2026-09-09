package com.commerceops.admin.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderRefundRequest(
        @NotBlank(message = "Refund reason is required.")
        @Size(max = 1000, message = "Refund reason must not exceed 1000 characters.")
        String reason
) {
}
