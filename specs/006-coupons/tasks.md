# Tasks: Coupons

## Backend

- [ ] Create `Coupon` entity.
- [ ] Create coupon request and response DTOs.
- [ ] Create coupon repository.
- [ ] Create coupon service.
- [ ] Create coupon controller.
- [ ] Add coupon listing with filters and pagination.
- [ ] Add coupon activation endpoint.
- [ ] Add coupon deactivation endpoint.
- [ ] Add coupon soft delete.
- [ ] Normalize coupon codes to uppercase.

## Database And Flyway

- [ ] Create `coupon` table migration.
- [ ] Add unique constraint or partial unique index for coupon code.
- [ ] Add unique constraint for coupon `public_id`.
- [ ] Add indexes for coupon `status`, `code`, and validity dates.

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

