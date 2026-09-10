package com.commerceops.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DashboardSummaryResponse(
        DashboardPeriodResponse period,
        DashboardMetricsResponse metrics,
        @Schema(description = "Up to five most recently created non-deleted orders in the resolved period")
        List<RecentOrderSummaryResponse> recentOrders,
        @Schema(description = "Up to five current low-stock products, ordered by stock quantity then name")
        List<LowStockProductSummaryResponse> lowStockProducts
) {
}
