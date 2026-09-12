package com.commerceops.admin.audit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditRequestContext {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    public RequestDetails current() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return new RequestDetails(null, null, null);
        }

        HttpServletRequest request = attributes.getRequest();
        return new RequestDetails(
                trimToLength(request.getHeader(TRACE_ID_HEADER), 100),
                trimToLength(request.getMethod(), 20),
                trimToLength(request.getRequestURI(), 1000)
        );
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    public record RequestDetails(String traceId, String method, String path) {
    }
}
