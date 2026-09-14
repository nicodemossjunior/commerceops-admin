package com.commerceops.admin.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error associated with one request field")
public record FieldErrorResponse(
        @Schema(description = "Invalid field name", example = "name") String field,
        @Schema(description = "Validation message", example = "Product name is required.") String message
) {
}
