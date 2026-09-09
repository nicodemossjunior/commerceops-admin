# Tasks: Orders

## Backend

- [x] Create `SalesOrder` entity.
- [x] Create `OrderItem` entity.
- [x] Create `OrderStatusHistory` entity.
- [x] Create order response DTOs.
- [x] Create status update request DTO.
- [x] Create cancel request DTO.
- [x] Create refund request DTO.
- [x] Create order repository.
- [x] Create order status history repository.
- [x] Create order service.
- [x] Create order status transition service.
- [x] Create order controller.
- [x] Add order filters and pagination.
- [x] Add order status history recording.

## Database And Flyway

- [x] Create `sales_order` table migration.
- [x] Create `order_item` table migration.
- [x] Create `order_status_history` table migration.
- [x] Add unique constraint for order `public_id`.
- [x] Add unique constraint for `order_number`.
- [x] Add indexes for `customer_id`, `status`, and `created_at`.
- [x] Add indexes for order item `sales_order_id` and `product_id`.

## Tests

- [x] Test order listing.
- [x] Test order detail.
- [x] Test valid status transition.
- [x] Test invalid status transition.
- [x] Test cancellation.
- [x] Test refund.
- [x] Test status history creation.
- [x] Test role-based access.

## Documentation

- [x] Document order endpoints in OpenAPI.
- [x] Document order status values.
- [x] Document status transition rules.
- [x] Document cancellation and refund behavior.

## Validation

- [x] Run unit tests.
- [x] Run controller tests.
- [x] Run repository integration tests.
- [x] Run migration validation.
