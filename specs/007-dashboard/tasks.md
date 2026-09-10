# Tasks: Dashboard

## Backend

- [ ] Create dashboard summary response DTO.
- [ ] Create dashboard metric DTOs.
- [ ] Create recent order summary DTO.
- [ ] Create low-stock product summary DTO.
- [ ] Create dashboard service.
- [ ] Create dashboard controller.
- [x] Add period parsing.
- [ ] Add configurable low-stock threshold.
- [ ] Add revenue metric calculations.
- [ ] Add order metric calculations.
- [ ] Add customer count metric.
- [x] Add low-stock product query.

## Database

- [x] Review indexes required for dashboard queries.
- [x] Add indexes for order date and status if not already covered.
- [x] Add indexes for product stock and status if not already covered.

## Tests

- [ ] Test dashboard summary default period.
- [ ] Test dashboard summary custom period.
- [ ] Test invalid custom period.
- [ ] Test revenue calculations.
- [ ] Test average order value calculation.
- [ ] Test cancellation and refund indicators.
- [ ] Test low-stock products.
- [ ] Test role-based access.

## Documentation

- [ ] Document dashboard endpoint in OpenAPI.
- [ ] Document period filter values.
- [ ] Document metric meanings.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests for dashboard queries.
