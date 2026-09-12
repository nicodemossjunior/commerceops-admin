package com.commerceops.admin.observability;

import com.commerceops.admin.auth.security.AdminUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final int MAX_CORRELATION_ID_LENGTH = 100;
    private static final Set<String> MANAGED_MDC_KEYS = Set.of(
            "requestId", "traceId", "userId", "method", "path", "status", "durationMs"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String requestId = resolveCorrelationId(request.getHeader(CorrelationContext.REQUEST_ID_HEADER));
        String traceId = resolveCorrelationId(request.getHeader(CorrelationContext.TRACE_ID_HEADER));

        request.setAttribute(CorrelationContext.REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(CorrelationContext.TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(CorrelationContext.REQUEST_ID_HEADER, requestId);
        response.setHeader(CorrelationContext.TRACE_ID_HEADER, traceId);

        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            addAuthenticatedUser();
            MDC.put("status", Integer.toString(response.getStatus()));
            MDC.put("durationMs", Long.toString(
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
            ));
            LOGGER.info("HTTP request completed");
            MANAGED_MDC_KEYS.forEach(MDC::remove);
        }
    }

    private String resolveCorrelationId(String suppliedValue) {
        if (suppliedValue != null) {
            String candidate = suppliedValue.trim();
            if (!candidate.isEmpty()
                    && candidate.length() <= MAX_CORRELATION_ID_LENGTH
                    && candidate.matches("[A-Za-z0-9._-]+")) {
                return candidate;
            }
        }
        return UUID.randomUUID().toString();
    }

    private void addAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserPrincipal principal) {
            MDC.put("userId", principal.publicId().toString());
        }
    }
}
