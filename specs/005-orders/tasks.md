# Tasks: Orders

## Backend

- [ ] Create `SalesOrder` entity.
- [ ] Create `OrderItem` entity.
- [ ] Create `OrderStatusHistory` entity.
- [ ] Create order response DTOs.
- [ ] Create status update request DTO.
- [ ] Create cancel request DTO.
- [ ] Create refund request DTO.
- [ ] Create order repository.
- [ ] Create order status history repository.
- [ ] Create order service.
- [ ] Create order status transition service.
- [ ] Create order controller.
- [ ] Add order filters and pagination.
- [ ] Add order status history recording.

## Database And Flyway

- [ ] Create `sales_order` table migration.
- [ ] Create `order_item` table migration.
- [ ] Create `order_status_history` table migration.
- [ ] Add unique constraint for order `public_id`.
- [ ] Add unique constraint for `order_number`.
- [ ] Add indexes for `customer_id`, `status`, and `created_at`.
- [ ] Add indexes for order item `sales_order_id` and `product_id`.

## Tests

- [ ] Test order listing.
- [ ] Test order detail.
- [ ] Test valid status transition.
- [ ] Test invalid status transition.
- [ ] Test cancellation.
- [ ] Test refund.
- [ ] Test status history creation.
- [ ] Test role-based access.

## Documentation

- [ ] Document order endpoints in OpenAPI.
- [ ] Document order status values.
- [ ] Document status transition rules.
- [ ] Document cancellation and refund behavior.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.

