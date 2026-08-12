# Tasks: Catalog

## Categories

- [ ] Create `Category` entity.
- [ ] Create category request and response DTOs.
- [ ] Create category repository.
- [ ] Create category service.
- [ ] Create category controller.
- [ ] Add category listing with pagination.
- [ ] Add category soft delete.
- [ ] Add validation for unique slug.

## Products

- [ ] Create `Product` entity.
- [ ] Create product request and response DTOs.
- [ ] Create product repository.
- [ ] Create product service.
- [ ] Create product controller.
- [ ] Add product listing with pagination.
- [ ] Add product filters.
- [ ] Add product soft delete.
- [ ] Add validation for unique SKU.
- [ ] Add validation for price and stock quantity.

## Database And Flyway

- [ ] Create `category` table migration.
- [ ] Create `product` table migration.
- [ ] Add indexes for category `public_id`, `slug`, and `status`.
- [ ] Add indexes for product `public_id`, `sku`, `status`, and `category_id`.
- [ ] Add unique constraints for active category slug and product SKU.

## Tests

- [ ] Test category creation.
- [ ] Test category update.
- [ ] Test category soft delete.
- [ ] Test product creation.
- [ ] Test product update.
- [ ] Test product soft delete.
- [ ] Test product filtering.
- [ ] Test duplicate SKU rejection.
- [ ] Test role-based access.

## Documentation

- [ ] Document category endpoints in OpenAPI.
- [ ] Document product endpoints in OpenAPI.
- [ ] Document product filters.
- [ ] Document product status values.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.

