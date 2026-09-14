package com.commerceops.admin.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Stable error response returned by every business API endpoint")
public record ApiErrorResponse(
        @Schema(description = "Error occurrence time in ISO 8601 UTC", example = "2026-08-12T16:21:00Z") Instant timestamp,
        @Schema(description = "HTTP status code", example = "400") int status,
        @Schema(description = "HTTP status reason", example = "Bad Request") String error,
        @Schema(description = "Stable machine-readable application error code", example = "VALIDATION_ERROR") ApiErrorCode code,
        @Schema(description = "Human-readable error explanation", example = "Request validation failed.") String message,
        @Schema(description = "Request path without a query string", example = "/api/products") String path,
        @Schema(description = "Trace identifier for support correlation", example = "8f3a1c0e4c9b4b2a") String traceId,
        @Schema(description = "Field-level validation details; empty for non-validation errors") List<FieldErrorResponse> fieldErrors
) {

    public static ApiErrorResponse withoutFieldErrors(
            int status,
            String error,
            ApiErrorCode code,
            String message,
            String path,
            String traceId
    ) {
        return new ApiErrorResponse(Instant.now(), status, error, code, message, path, traceId, List.of());
    }
}
