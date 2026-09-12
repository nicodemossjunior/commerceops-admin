package com.commerceops.admin.observability;

import jakarta.servlet.http.HttpServletRequest;

public final class CorrelationContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_ATTRIBUTE = CorrelationContext.class.getName() + ".requestId";
    public static final String TRACE_ID_ATTRIBUTE = CorrelationContext.class.getName() + ".traceId";

    private CorrelationContext() {
    }

    public static String requestId(HttpServletRequest request) {
        return attributeOrHeader(request, REQUEST_ID_ATTRIBUTE, REQUEST_ID_HEADER);
    }

    public static String traceId(HttpServletRequest request) {
        return attributeOrHeader(request, TRACE_ID_ATTRIBUTE, TRACE_ID_HEADER);
    }

    private static String attributeOrHeader(HttpServletRequest request, String attributeName, String headerName) {
        Object attribute = request.getAttribute(attributeName);
        if (attribute instanceof String value && !value.isBlank()) {
            return value;
        }
        String header = request.getHeader(headerName);
        return header == null || header.isBlank() ? null : header.trim();
    }
}
