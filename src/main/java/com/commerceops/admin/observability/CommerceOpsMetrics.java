package com.commerceops.admin.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CommerceOpsMetrics {

    private final Counter ordersCreated;
    private final Counter loginSuccess;
    private final Counter loginFailure;

    public CommerceOpsMetrics(MeterRegistry meterRegistry) {
        ordersCreated = Counter.builder("commerceops.orders.created")
                .description("Number of orders created")
                .register(meterRegistry);
        loginSuccess = Counter.builder("commerceops.auth.login.success")
                .description("Number of successful authentication attempts")
                .register(meterRegistry);
        loginFailure = Counter.builder("commerceops.auth.login.failure")
                .description("Number of failed authentication attempts")
                .register(meterRegistry);
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    public void recordLoginSuccess() {
        loginSuccess.increment();
    }

    public void recordLoginFailure() {
        loginFailure.increment();
    }
}
