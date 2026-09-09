# Tasks: Coupons

## Backend

- [x] Create `Coupon` entity.
- [x] Create coupon request and response DTOs.
- [x] Create coupon repository.
- [x] Create coupon service.
- [x] Create coupon controller.
- [x] Add coupon listing with filters and pagination.
- [x] Add coupon activation endpoint.
- [x] Add coupon deactivation endpoint.
- [x] Add coupon soft delete.
- [x] Normalize coupon codes to uppercase.

## Database And Flyway

- [x] Create `coupon` table migration.
- [x] Add unique constraint or partial unique index for coupon code.
- [x] Add unique constraint for coupon `public_id`.
- [x] Add indexes for coupon `status`, `code`, and validity dates.

## Tests

- [x] Test coupon creation.
- [x] Test coupon update.
- [x] Test coupon soft delete.
- [x] Test duplicate code rejection.
- [x] Test fixed amount discount validation.
- [x] Test percentage discount validation.
- [x] Test date range validation.
- [x] Test activation and deactivation.
- [x] Test role-based access.

## Documentation

- [x] Document coupon endpoints in OpenAPI.
- [x] Document discount type values.
- [x] Document coupon status values.
- [x] Document coupon validation rules.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.
