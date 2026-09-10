# Tasks: Dashboard

## Backend

- [x] Create dashboard summary response DTO.
- [x] Create dashboard metric DTOs.
- [x] Create recent order summary DTO.
- [x] Create low-stock product summary DTO.
- [x] Create dashboard service.
- [x] Create dashboard controller.
- [x] Add period parsing.
- [x] Add configurable low-stock threshold.
- [x] Add revenue metric calculations.
- [x] Add order metric calculations.
- [x] Add customer count metric.
- [x] Add low-stock product query.

## Database

- [x] Review indexes required for dashboard queries.
- [x] Add indexes for order date and status if not already covered.
- [x] Add indexes for product stock and status if not already covered.

## Tests

- [x] Test dashboard summary default period.
- [x] Test dashboard summary custom period.
- [x] Test invalid custom period.
- [x] Test revenue calculations.
- [x] Test average order value calculation.
- [x] Test cancellation and refund indicators.
- [x] Test low-stock products.
- [x] Test role-based access.

## Documentation

- [x] Document dashboard endpoint in OpenAPI.
- [x] Document period filter values.
- [x] Document metric meanings.

## Validation

- [x] Run unit tests.
- [x] Run controller tests.
- [x] Run repository integration tests for dashboard queries.
