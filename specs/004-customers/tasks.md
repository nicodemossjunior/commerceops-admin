# Tasks: Customers

## Backend

- [ ] Create `Customer` entity.
- [ ] Create `CustomerNote` entity.
- [ ] Create customer request and response DTOs.
- [ ] Create customer note request and response DTOs.
- [ ] Create customer repository.
- [ ] Create customer note repository.
- [ ] Create customer service.
- [ ] Create customer note service.
- [ ] Create customer controller.
- [ ] Add customer search and pagination.
- [ ] Add customer soft delete.
- [ ] Add customer note soft delete.
- [ ] Add purchase history endpoint contract.

## Database And Flyway

- [ ] Create `customer` table migration.
- [ ] Create `customer_note` table migration.
- [ ] Add unique constraint for customer `public_id`.
- [ ] Add unique constraint or partial unique index for customer email.
- [ ] Add indexes for customer `email`, `status`, and `created_at`.
- [ ] Add index for customer note `customer_id`.

## Tests

- [ ] Test customer creation.
- [ ] Test customer update.
- [ ] Test customer listing filters.
- [ ] Test customer soft delete.
- [ ] Test duplicate email rejection.
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

