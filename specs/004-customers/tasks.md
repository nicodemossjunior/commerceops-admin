# Tasks: Customers

## Backend

- [x] Create `Customer` entity.
- [x] Create `CustomerNote` entity.
- [x] Create customer request and response DTOs.
- [ ] Create customer note request and response DTOs.
- [x] Create customer repository.
- [x] Create customer note repository.
- [x] Create customer service.
- [ ] Create customer note service.
- [x] Create customer controller.
- [x] Add customer search and pagination.
- [x] Add customer soft delete.
- [ ] Add customer note soft delete.
- [ ] Add purchase history endpoint contract.

## Database And Flyway

- [x] Create `customer` table migration.
- [x] Create `customer_note` table migration.
- [x] Add unique constraint for customer `public_id`.
- [x] Add unique constraint or partial unique index for customer email.
- [x] Add indexes for customer `email`, `status`, and `created_at`.
- [x] Add index for customer note `customer_id`.

## Tests

- [x] Test customer creation.
- [x] Test customer update.
- [x] Test customer listing filters.
- [x] Test customer soft delete.
- [x] Test duplicate email rejection.
- [ ] Test customer note creation.
- [ ] Test customer note soft delete.
- [ ] Test role-based access.

## Documentation

- [ ] Document customer endpoints in OpenAPI.
- [ ] Document customer note endpoints in OpenAPI.
- [ ] Document customer status values.
- [ ] Document purchase history endpoint behavior.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.
