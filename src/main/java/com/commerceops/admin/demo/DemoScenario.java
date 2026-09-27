package com.commerceops.admin.demo;

import com.commerceops.admin.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pure, reusable scenario definitions. No database or application startup side effects. */
public final class DemoScenario {
    public static final int VERSION = 1;
    private DemoScenario() { }

    public record Order(int index, int product, int customer, int quantity, BigDecimal price,
                        BigDecimal discount, BigDecimal shipping, Instant createdAt, OrderStatus status) {
        public BigDecimal subtotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
        public BigDecimal total() { return subtotal().subtract(discount).add(shipping); }
    }

    public static BigDecimal price(int product) {
        return BigDecimal.valueOf(1000L + product * 137L, 2);
    }

    public static List<Order> orders(long seed, Instant reference) {
        Random random = new Random(seed);
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            int product = random.nextInt(100);
            // Includes both exact custom-period boundaries; the last 50 customers have no orders.
            Instant created = reference.minusSeconds((i % 90) * 86400L);
            orders.add(new Order(i, product, i % 150, 1 + random.nextInt(4), price(product),
                    BigDecimal.valueOf(i % 5), BigDecimal.valueOf(i % 3 * 5L), created,
                    OrderStatus.values()[i % OrderStatus.values().length]));
        }
        return List.copyOf(orders);
    }

    public static List<OrderStatus> transitions(OrderStatus target) {
        return switch (target) {
            case PENDING -> List.of();
            case CANCELLED -> List.of(OrderStatus.CANCELLED);
            case REFUNDED -> List.of(OrderStatus.PAID, OrderStatus.REFUNDED);
            default -> List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED).subList(0, target.ordinal());
        };
    }

    public record Metrics(BigDecimal grossRevenue, BigDecimal netRevenue, long orderCount,
                          BigDecimal averageOrderValue, long cancelledOrderCount, long refundedOrderCount) { }

    public static Metrics expected(List<Order> orders, Instant from, Instant to) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        long count = 0;
        long cancelled = 0;
        long refunded = 0;
        for (Order order : orders) {
            if (order.createdAt().isBefore(from) || order.createdAt().isAfter(to)) { continue; }
            count++;
            if (order.status() == OrderStatus.CANCELLED) { cancelled++; continue; }
            gross = gross.add(order.total());
            if (order.status() == OrderStatus.REFUNDED) { refunded++; continue; }
            net = net.add(order.total());
        }
        long denominator = count - cancelled - refunded;
        return new Metrics(gross.setScale(2), net.setScale(2), count,
                denominator == 0 ? new BigDecimal("0.00")
                        : net.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP), cancelled, refunded);
    }
}
