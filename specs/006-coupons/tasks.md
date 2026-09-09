# Tasks: Coupons

## Backend

- [x] Create `Coupon` entity.
- [ ] Create coupon request and response DTOs.
- [x] Create coupon repository.
- [ ] Create coupon service.
- [ ] Create coupon controller.
- [ ] Add coupon listing with filters and pagination.
- [ ] Add coupon activation endpoint.
- [ ] Add coupon deactivation endpoint.
- [ ] Add coupon soft delete.
- [ ] Normalize coupon codes to uppercase.

## Database And Flyway

- [x] Create `coupon` table migration.
- [x] Add unique constraint or partial unique index for coupon code.
- [x] Add unique constraint for coupon `public_id`.
- [x] Add indexes for coupon `status`, `code`, and validity dates.

## Tests

- [ ] Test coupon creation.
- [ ] Test coupon update.
- [ ] Test coupon soft delete.
- [ ] Test duplicate code rejection.
- [ ] Test fixed amount discount validation.
- [ ] Test percentage discount validation.
- [ ] Test date range validation.
- [ ] Test activation and deactivation.
- [ ] Test role-based access.

## Documentation

- [ ] Document coupon endpoints in OpenAPI.
- [ ] Document discount type values.
- [ ] Document coupon status values.
- [ ] Document coupon validation rules.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.
