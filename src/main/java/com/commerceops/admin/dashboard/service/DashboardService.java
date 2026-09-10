package com.commerceops.admin.dashboard.service;

import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.customers.repository.CustomerRepository;
import com.commerceops.admin.dashboard.dto.DashboardMetricsResponse;
import com.commerceops.admin.dashboard.dto.DashboardPeriodResponse;
import com.commerceops.admin.dashboard.dto.DashboardSummaryResponse;
import com.commerceops.admin.dashboard.dto.LowStockProductSummaryResponse;
import com.commerceops.admin.dashboard.dto.RecentOrderSummaryResponse;
import com.commerceops.admin.dashboard.model.DashboardPeriod;
import com.commerceops.admin.dashboard.model.DashboardPeriodShortcut;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.OrderDashboardMetricsProjection;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int SUPPORTING_LIST_SIZE = 5;

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final DashboardPeriodResolver periodResolver;
    private final int lowStockThreshold;

    public DashboardService(
            SalesOrderRepository salesOrderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            DashboardPeriodResolver periodResolver,
            @Value("${commerceops.dashboard.low-stock-threshold:10}") int lowStockThreshold
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.periodResolver = periodResolver;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(
            DashboardPeriodShortcut shortcut,
            Instant from,
            Instant to
    ) {
        DashboardPeriod period = periodResolver.resolve(shortcut, from, to);
        OrderDashboardMetricsProjection orderMetrics = salesOrderRepository.summarizeDashboardMetrics(
                period.from(),
                period.to(),
                OrderStatus.CANCELLED,
                OrderStatus.REFUNDED
        );

        long netOrderCount = Math.max(
                0,
                orderMetrics.getOrderCount()
                        - orderMetrics.getCancelledOrderCount()
                        - orderMetrics.getRefundedOrderCount()
        );
        BigDecimal netRevenue = money(orderMetrics.getNetRevenue());
        DashboardMetricsResponse metrics = new DashboardMetricsResponse(
                money(orderMetrics.getGrossRevenue()),
                netRevenue,
                orderMetrics.getOrderCount(),
                average(netRevenue, netOrderCount),
                customerRepository.countByDeletedFalse(),
                orderMetrics.getCancelledOrderCount(),
                orderMetrics.getRefundedOrderCount(),
                productRepository.countByStockQuantityLessThanEqualAndDeletedFalse(lowStockThreshold)
        );

        var recentOrders = salesOrderRepository
                .findByDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(
                        period.from(),
                        period.to(),
                        PageRequest.of(0, SUPPORTING_LIST_SIZE)
                )
                .map(this::toRecentOrder)
                .getContent();
        var lowStockProducts = productRepository
                .findByStockQuantityLessThanEqualAndDeletedFalseOrderByStockQuantityAscNameAsc(
                        lowStockThreshold,
                        PageRequest.of(0, SUPPORTING_LIST_SIZE)
                )
                .map(this::toLowStockProduct)
                .getContent();

        return new DashboardSummaryResponse(
                new DashboardPeriodResponse(period.from(), period.to()),
                metrics,
                recentOrders,
                lowStockProducts
        );
    }

    private BigDecimal average(BigDecimal netRevenue, long orderCount) {
        if (orderCount == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return netRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private RecentOrderSummaryResponse toRecentOrder(SalesOrder order) {
        return new RecentOrderSummaryResponse(
                order.getPublicId(),
                order.getOrderNumber(),
                order.getCustomer().getPublicId(),
                order.getCustomer().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }

    private LowStockProductSummaryResponse toLowStockProduct(Product product) {
        return new LowStockProductSummaryResponse(
                product.getPublicId(),
                product.getSku(),
                product.getName(),
                product.getStockQuantity(),
                product.getStatus()
        );
    }
}
