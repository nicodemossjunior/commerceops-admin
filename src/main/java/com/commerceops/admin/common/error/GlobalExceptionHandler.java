package com.commerceops.admin.common.error;

import com.commerceops.admin.auth.service.AuthenticationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                java.time.Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR,
                "Request validation failed.",
                request.getRequestURI(),
                traceId(request),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({RequestValidationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleRequestValidation(
            Exception exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, exception.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, ApiErrorCode.DUPLICATE_RESOURCE, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorCode.BUSINESS_RULE_VIOLATION, exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailed(
            AuthenticationFailedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTHENTICATION_FAILED, exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.FORBIDDEN, ApiErrorCode.ACCESS_DENIED, "Access denied.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request
        );
    }

    private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
        return new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.withoutFieldErrors(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                traceId(request)
        );

        return ResponseEntity.status(status).body(response);
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        return traceId == null || traceId.isBlank() ? null : traceId;
    }
}
