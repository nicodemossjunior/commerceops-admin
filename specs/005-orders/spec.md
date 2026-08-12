# Spec: Orders

## Objective

Implement order management for CommerceOps Admin, including order listing, order detail, items, customer association, status transitions, change history, cancellation, and refund tracking.

## Scope

- List orders with filters and pagination.
- View order details.
- Store order items.
- Associate orders with customers.
- Track payment and delivery summary data.
- Update order status through controlled transitions.
- Store order status history.
- Support cancellation and refund metadata when applicable.
- Apply hybrid ID strategy.
- Apply soft delete only where operationally appropriate.

## Out Of Scope

- Checkout flow.
- Payment gateway integration.
- Shipping provider integration.
- Public customer order tracking.
- Invoice generation.
- Advanced refund automation.

## Order Status

```text
PENDING
PAID
PROCESSING
SHIPPED
DELIVERED
CANCELLED
REFUNDED
```

## Business Rules

- Orders must have at least one item.
- Order total must match the sum of item totals plus adjustments.
- Order status changes must be recorded in history.
- Cancelled orders cannot move back to active fulfillment statuses.
- Refunded orders cannot move back to paid or fulfillment statuses.
- Status transitions must be validated by business rules.
- Deleted products should not break historical order item display.

## Data Model

Tables:

```text
sales_order
order_item
order_status_history
```

`sales_order` fields:

```text
id
public_id
customer_id
order_number
status
subtotal_amount
discount_amount
shipping_amount
total_amount
payment_status
delivery_status
cancelled_at
refunded_at
created_at
updated_at
deleted
deleted_at
deleted_by
```

`order_item` fields:

```text
id
public_id
sales_order_id
product_id
product_sku
product_name
unit_price
quantity
total_amount
created_at
updated_at
```

`order_status_history` fields:

```text
id
public_id
sales_order_id
from_status
to_status
changed_by
reason
created_at
```

## API Contracts

```text
GET   /api/orders
GET   /api/orders/{publicId}
PATCH /api/orders/{publicId}/status
POST  /api/orders/{publicId}/cancel
POST  /api/orders/{publicId}/refund
```

Order filters:

```text
orderNumber
customerId
status
paymentStatus
deliveryStatus
createdFrom
createdTo
page
size
sort
```

Status update request:

```json
{
  "status": "PROCESSING",
  "reason": "Payment confirmed and order is ready for fulfillment."
}
```

## Validation

- Order status is required for status updates.
- Status transition must be allowed.
- Reason is required for cancellation and refund.
- Order must exist and must not be deleted.
- Customer must exist when order is created by internal/admin flows.

## Authorization

- `ADMIN` can perform all order operations.
- `MANAGER` can manage order statuses, cancellations, and refunds.
- `SUPPORT` can read orders and request operational actions where allowed.
- `READ_ONLY` can read orders.
- `CATALOG` has no order write access.

## Expected Errors

- `VALIDATION_ERROR` for invalid payloads.
- `RESOURCE_NOT_FOUND` when order does not exist.
- `BUSINESS_RULE_VIOLATION` for invalid transitions.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Orders can be listed with filters and pagination.
- Order details include customer, items, totals, payment summary, delivery summary, and status history.
- Order status can be changed only through valid transitions.
- Cancellation and refund operations are tracked.
- Status history is immutable from the API perspective.

## Expected Tests

- Order listing tests.
- Order detail tests.
- Status transition tests.
- Invalid transition tests.
- Cancellation tests.
- Refund tests.
- Authorization tests.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.
- `003-catalog`.
- `004-customers`.

