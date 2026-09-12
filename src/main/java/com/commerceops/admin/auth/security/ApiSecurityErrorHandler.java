package com.commerceops.admin.auth.security;

import com.commerceops.admin.common.error.ApiErrorCode;
import com.commerceops.admin.common.error.ApiErrorResponse;
import com.commerceops.admin.observability.CorrelationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        writeError(response, request, HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTHENTICATION_FAILED, "Authentication is required.");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        writeError(response, request, HttpStatus.FORBIDDEN, ApiErrorCode.ACCESS_DENIED, "Access denied.");
    }

    private void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            ApiErrorCode code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.withoutFieldErrors(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                traceId(request)
        ));
    }

    private String traceId(HttpServletRequest request) {
        return CorrelationContext.traceId(request);
    }
}
