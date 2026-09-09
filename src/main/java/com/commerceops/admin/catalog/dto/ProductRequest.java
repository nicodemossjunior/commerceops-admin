package com.commerceops.admin.catalog.dto;

import com.commerceops.admin.catalog.model.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotNull(message = "Product category is required.")
        UUID categoryPublicId,

        @NotBlank(message = "Product SKU is required.")
        @Size(max = 80, message = "Product SKU must not exceed 80 characters.")
        String sku,

        @NotBlank(message = "Product name is required.")
        @Size(max = 180, message = "Product name must not exceed 180 characters.")
        String name,

        @NotBlank(message = "Product slug is required.")
        @Size(max = 200, message = "Product slug must not exceed 200 characters.")
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "Product slug must contain only lowercase letters, numbers, and hyphens."
        )
        String slug,

        @Size(max = 2000, message = "Product description must not exceed 2000 characters.")
        String description,

        @NotNull(message = "Product price is required.")
        @DecimalMin(value = "0.00", message = "Product price cannot be negative.")
        BigDecimal price,

        @Size(max = 2048, message = "Product image URL must not exceed 2048 characters.")
        String imageUrl,

        @NotNull(message = "Product stock quantity is required.")
        @Min(value = 0, message = "Product stock quantity cannot be negative.")
        Integer stockQuantity,

        @NotNull(message = "Product status is required.")
        ProductStatus status
) {
}
