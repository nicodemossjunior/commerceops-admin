package com.commerceops.admin.observability;

import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.coupons.dto.CouponFilter;
import com.commerceops.admin.coupons.repository.CouponRepository;
import com.commerceops.admin.coupons.repository.CouponSpecifications;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainMetricsConfiguration {

    public DomainMetricsConfiguration(
            MeterRegistry meterRegistry,
            ProductRepository productRepository,
            CouponRepository couponRepository,
            SalesOrderRepository salesOrderRepository,
            @Value("${commerceops.dashboard.low-stock-threshold:10}") int lowStockThreshold
    ) {
        Gauge.builder(
                        "commerceops.products.low_stock",
                        productRepository,
                        repository -> repository.countByStockQuantityLessThanEqualAndDeletedFalse(lowStockThreshold)
                )
                .description("Current number of non-deleted low-stock products")
                .register(meterRegistry);

        Gauge.builder(
                        "commerceops.coupons.active",
                        couponRepository,
                        this::countActiveCoupons
                )
                .description("Current number of eligible active coupons")
                .register(meterRegistry);

        for (OrderStatus status : OrderStatus.values()) {
            Gauge.builder(
                            "commerceops.orders.by_status",
                            salesOrderRepository,
                            repository -> repository.countByStatusAndDeletedFalse(status)
                    )
                    .tag("status", status.name())
                    .description("Current number of non-deleted orders by status")
                    .register(meterRegistry);
        }
    }

    private double countActiveCoupons(CouponRepository couponRepository) {
        Instant now = Instant.now();
        return couponRepository.count(CouponSpecifications.withFilters(
                new CouponFilter(null, null, null, now),
                now
        ));
    }
}
