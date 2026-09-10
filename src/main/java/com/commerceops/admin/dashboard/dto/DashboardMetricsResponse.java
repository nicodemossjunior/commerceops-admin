package com.commerceops.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record DashboardMetricsResponse(
        @Schema(description = "Sum of non-cancelled order totals in the period, including refunded orders")
        BigDecimal grossRevenue,
        @Schema(description = "Sum of order totals in the period, excluding cancelled and refunded orders")
        BigDecimal netRevenue,
        @Schema(description = "Number of non-deleted orders created in the period, regardless of status")
        long orderCount,
        @Schema(description = "Net revenue divided by non-cancelled and non-refunded orders in the period")
        BigDecimal averageOrderValue,
        @Schema(description = "Current total number of non-deleted customers")
        long customerCount,
        @Schema(description = "Number of cancelled orders created in the period")
        long cancelledOrderCount,
        @Schema(description = "Number of refunded orders created in the period")
        long refundedOrderCount,
        @Schema(description = "Current number of non-deleted products at or below the configured stock threshold")
        long lowStockProductCount
) {
}
