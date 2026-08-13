package com.commerceops.admin.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        ApiErrorCode code,
        String message,
        String path,
        String traceId,
        List<FieldErrorResponse> fieldErrors
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
