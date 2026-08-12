# Spec: Dashboard

## Objective

Implement the operational dashboard API for CommerceOps Admin, exposing summary metrics for sales, orders, customers, stock, cancellations, refunds, and average order value.

## Scope

- Provide a dashboard summary endpoint.
- Show revenue summary.
- Show recent orders.
- Show low-stock products.
- Show total customers.
- Show average order value.
- Show cancellation and refund indicators.
- Support period filters.
- Return data optimized for an administrative frontend.

## Out Of Scope

- Frontend dashboard UI.
- Advanced analytics.
- Data warehouse integration.
- Forecasting.
- Real-time streaming updates.
- Exportable reports.

## Metrics

Initial dashboard metrics:

```text
grossRevenue
netRevenue
orderCount
averageOrderValue
customerCount
cancelledOrderCount
refundedOrderCount
lowStockProductCount
```

Supporting lists:

```text
recentOrders
lowStockProducts
```

## API Contracts

```text
GET /api/dashboard/summary
```

Filters:

```text
period
from
to
```

Supported period shortcuts:

```text
TODAY
LAST_7_DAYS
LAST_30_DAYS
THIS_MONTH
CUSTOM
```

Example response:

```json
{
  "period": {
    "from": "2026-08-01T00:00:00Z",
    "to": "2026-08-12T23:59:59Z"
  },
  "metrics": {
    "grossRevenue": 12500.00,
    "netRevenue": 11800.00,
    "orderCount": 132,
    "averageOrderValue": 94.69,
    "customerCount": 87,
    "cancelledOrderCount": 4,
    "refundedOrderCount": 2,
    "lowStockProductCount": 9
  },
  "recentOrders": [],
  "lowStockProducts": []
}
```

## Business Rules

- Dashboard values must be derived from persisted domain data.
- Date filters must be inclusive and timezone-safe.
- Default period should be `LAST_30_DAYS`.
- Low-stock threshold should be configurable.
- Deleted records should be excluded unless historical order data requires otherwise.
- Cancelled and refunded indicators must be based on order status.

## Validation

- `from` and `to` are required when period is `CUSTOM`.
- `from` must be before or equal to `to`.
- Invalid period values return validation errors.

## Authorization

- `ADMIN`, `MANAGER`, and `READ_ONLY` can access dashboard summary.
- `SUPPORT` may access dashboard summary if operational visibility is desired.
- `CATALOG` may access low-stock data through catalog-specific endpoints, not necessarily the full dashboard.

## Expected Errors

- `VALIDATION_ERROR` for invalid date filters.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Dashboard summary endpoint returns stable response structure.
- Metrics are calculated from orders, customers, and products.
- Default period works without query parameters.
- Custom date range works with `from` and `to`.
- Low-stock products are returned using a configurable threshold.
- Endpoint is protected by role-based authorization.

## Expected Tests

- Dashboard summary default period test.
- Dashboard custom period test.
- Invalid date range test.
- Metric calculation tests.
- Low-stock products test.
- Authorization tests.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.
- `003-catalog`.
- `004-customers`.
- `005-orders`.

