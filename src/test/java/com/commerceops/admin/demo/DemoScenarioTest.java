package com.commerceops.admin.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.service.OrderStatusTransitionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DemoScenarioTest {
    @Test
    void generatesDeterministicValidOrdersAcrossNinetyDays() {
        Instant reference = Instant.parse("2026-09-26T12:00:00Z");
        var orders = DemoScenario.orders(42, reference);
        assertThat(orders).hasSize(500).isEqualTo(DemoScenario.orders(42, reference));
        assertThat(orders).isNotEqualTo(DemoScenario.orders(43, reference));
        assertThat(orders).extracting(DemoScenario.Order::status).contains(OrderStatus.values());
        assertThat(orders).extracting(DemoScenario.Order::createdAt)
                .contains(reference, reference.minusSeconds(30L * 86400), reference.minusSeconds(89L * 86400));
        for (var order : orders) {
            assertThat(order.total()).isEqualByComparingTo(order.price().multiply(BigDecimal.valueOf(order.quantity()))
                    .subtract(order.discount()).add(order.shipping()));
            OrderStatus previous = OrderStatus.PENDING;
            for (OrderStatus next : DemoScenario.transitions(order.status())) {
                new OrderStatusTransitionService().validate(previous, next);
                previous = next;
            }
            assertThat(previous).isEqualTo(order.status());
        }
    }

    @Test
    void independentlyAccountsForRefundsCancellationsAndInclusiveBoundaries() {
        Instant at = Instant.parse("2026-09-26T12:00:00Z");
        var orders = List.of(order(at, OrderStatus.PAID), order(at, OrderStatus.REFUNDED),
                order(at, OrderStatus.CANCELLED), order(at.minusSeconds(1), OrderStatus.PENDING));
        var metrics = DemoScenario.expected(orders, at, at);
        assertThat(metrics.grossRevenue()).isEqualByComparingTo("200.00");
        assertThat(metrics.netRevenue()).isEqualByComparingTo("100.00");
        assertThat(metrics.averageOrderValue()).isEqualByComparingTo("100.00");
        assertThat(metrics.orderCount()).isEqualTo(3);
        assertThat(metrics.cancelledOrderCount()).isEqualTo(1);
        assertThat(metrics.refundedOrderCount()).isEqualTo(1);
        assertThat(DemoScenario.expected(orders, at.plusSeconds(1), at.plusSeconds(2)).orderCount()).isZero();
    }

    private DemoScenario.Order order(Instant at, OrderStatus status) {
        return new DemoScenario.Order(0, 0, 0, 1, new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO, at, status);
    }
}
