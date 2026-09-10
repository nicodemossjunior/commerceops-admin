package com.commerceops.admin.dashboard.dto;

import java.math.BigDecimal;

public record DashboardMetricsResponse(
        BigDecimal grossRevenue,
        BigDecimal netRevenue,
        long orderCount,
        BigDecimal averageOrderValue,
        long customerCount,
        long cancelledOrderCount,
        long refundedOrderCount,
        long lowStockProductCount
) {
}
