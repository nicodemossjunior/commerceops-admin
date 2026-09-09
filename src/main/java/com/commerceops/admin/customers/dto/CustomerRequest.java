package com.commerceops.admin.customers.dto;

import com.commerceops.admin.customers.model.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Customer name is required.")
        @Size(max = 160, message = "Customer name must not exceed 160 characters.")
        String name,

        @Email(message = "Customer email must be valid.")
        @Size(max = 190, message = "Customer email must not exceed 190 characters.")
        String email,

        @Pattern(
                regexp = "^\\+?[0-9 ()-]{7,30}$",
                message = "Customer phone must contain 7 to 30 valid phone characters."
        )
        String phone,

        @Size(max = 50, message = "Customer document must not exceed 50 characters.")
        String document,

        @NotNull(message = "Customer status is required.")
        @Schema(allowableValues = {"ACTIVE", "INACTIVE", "BLOCKED"})
        CustomerStatus status
) {
}
