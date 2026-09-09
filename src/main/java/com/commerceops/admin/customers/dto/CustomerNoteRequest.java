package com.commerceops.admin.customers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerNoteRequest(
        @NotBlank(message = "Customer note text is required.")
        @Size(max = 4000, message = "Customer note text must not exceed 4000 characters.")
        String note
) {
}
