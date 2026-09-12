package com.commerceops.admin.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CommerceOpsMetricsTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private CommerceOpsMetrics metrics;

    @Test
    void registersBoundedDomainMetersAndUpdatesAuthenticationCounters() {
        metrics.recordLoginSuccess();
        metrics.recordLoginFailure();

        assertThat(meterRegistry.get("commerceops.auth.login.success").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("commerceops.auth.login.failure").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("commerceops.orders.created").counter().count()).isZero();
        assertThat(meterRegistry.get("commerceops.products.low_stock").gauge().value()).isZero();
        assertThat(meterRegistry.get("commerceops.coupons.active").gauge().value()).isZero();
        assertThat(meterRegistry.find("commerceops.orders.by_status").gauges()).hasSize(7);
        assertThat(meterRegistry.find("commerceops.orders.by_status").gauges())
                .allSatisfy(gauge -> assertThat(gauge.getId().getTag("status")).isNotBlank());
    }
}
