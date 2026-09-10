package com.commerceops.admin.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
        DashboardPeriodResponse period,
        DashboardMetricsResponse metrics,
        List<RecentOrderSummaryResponse> recentOrders,
        List<LowStockProductSummaryResponse> lowStockProducts
) {
}
