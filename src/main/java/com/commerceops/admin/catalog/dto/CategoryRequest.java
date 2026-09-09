package com.commerceops.admin.catalog.dto;

import com.commerceops.admin.catalog.model.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CategoryRequest(
        @NotBlank(message = "Category name is required.")
        @Size(max = 120, message = "Category name must not exceed 120 characters.")
        String name,

        @NotBlank(message = "Category slug is required.")
        @Size(max = 160, message = "Category slug must not exceed 160 characters.")
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "Category slug must contain only lowercase letters, numbers, and hyphens."
        )
        String slug,

        @Size(max = 1000, message = "Category description must not exceed 1000 characters.")
        String description,

        @NotNull(message = "Category status is required.")
        CategoryStatus status,

        UUID parentPublicId
) {
}
