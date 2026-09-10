package com.commerceops.admin.orders.repository;

import java.math.BigDecimal;

public interface OrderDashboardMetricsProjection {

    BigDecimal getGrossRevenue();

    BigDecimal getNetRevenue();

    long getOrderCount();

    long getCancelledOrderCount();

    long getRefundedOrderCount();
}
