# Tasks: Orders

## Backend

- [x] Create `SalesOrder` entity.
- [x] Create `OrderItem` entity.
- [x] Create `OrderStatusHistory` entity.
- [ ] Create order response DTOs.
- [ ] Create status update request DTO.
- [ ] Create cancel request DTO.
- [ ] Create refund request DTO.
- [x] Create order repository.
- [x] Create order status history repository.
- [ ] Create order service.
- [ ] Create order status transition service.
- [ ] Create order controller.
- [ ] Add order filters and pagination.
- [ ] Add order status history recording.

## Database And Flyway

- [x] Create `sales_order` table migration.
- [x] Create `order_item` table migration.
- [x] Create `order_status_history` table migration.
- [x] Add unique constraint for order `public_id`.
- [x] Add unique constraint for `order_number`.
- [x] Add indexes for `customer_id`, `status`, and `created_at`.
- [x] Add indexes for order item `sales_order_id` and `product_id`.

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
