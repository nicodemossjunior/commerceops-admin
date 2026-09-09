# Tasks: Catalog

The implementation is divided into waves so that every intermediate state is reviewable, testable, and recorded in a focused commit. Complete the waves in order. Check each task only after its implementation and related tests pass.

## Wave 1 — Catalog Persistence Foundation

**Outcome:** The catalog database structure and persistence model exist, with constraints and indexes enforced by PostgreSQL.

**Planned commit:** `feat(catalog): add catalog persistence foundation`

- [x] Create `category` table migration.
- [x] Create `product` table migration.
- [x] Add indexes for category `public_id`, `slug`, and `status`.
- [x] Add indexes for product `public_id`, `sku`, `status`, and `category_id`.
- [x] Add unique constraints for active category slug and product SKU.
- [x] Create `Category` entity.
- [x] Create category repository.
- [x] Create `Product` entity.
- [x] Create product repository.
- [x] Run migration validation.

## Wave 2 — Category Read And Write Operations

**Outcome:** Authorized users can create, retrieve, update, and list categories through DTO-based, paginated API contracts.

**Planned commit:** `feat(catalog): implement category management`

- [x] Create category request and response DTOs.
- [x] Create category service.
- [x] Create category controller.
- [x] Add category listing with pagination.
- [x] Add validation for unique slug.
- [x] Test category creation.
- [x] Test category update.

## Wave 3 — Category Soft Delete

**Outcome:** Categories are soft deleted, excluded from default reads, and protected by catalog business rules.

**Planned commit:** `feat(catalog): add category soft delete`

- [x] Add category soft delete.
- [x] Test category soft delete.

## Wave 4 — Product Read And Write Operations

**Outcome:** Authorized users can create, retrieve, update, and list valid products through DTO-based, paginated API contracts.

**Planned commit:** `feat(catalog): implement product management`

- [x] Create product request and response DTOs.
- [x] Create product service.
- [x] Create product controller.
- [x] Add product listing with pagination.
- [x] Add validation for unique SKU.
- [x] Add validation for price and stock quantity.
- [x] Test product creation.
- [x] Test product update.
- [x] Test duplicate SKU rejection.

## Wave 5 — Product Filtering And Soft Delete

**Outcome:** Product searches support the specified filters, and deleted products are excluded from default reads.

**Planned commit:** `feat(catalog): add product filtering and soft delete`

- [ ] Add product filters.
- [ ] Add product soft delete.
- [ ] Test product filtering.
- [ ] Test product soft delete.

## Wave 6 — Authorization And API Documentation

**Outcome:** Catalog access follows the role matrix and all category and product contracts are discoverable in OpenAPI.

**Planned commit:** `test(catalog): verify access and document catalog API`

- [ ] Test role-based access.
- [ ] Document category endpoints in OpenAPI.
- [ ] Document product endpoints in OpenAPI.
- [ ] Document product filters.
- [ ] Document product status values.

## Wave 7 — Catalog Verification And Completion

**Outcome:** The complete catalog specification passes all required test suites and repository validation.

**Planned commit:** `chore(catalog): complete catalog specification`

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.

## Validation

Before creating the final commit for this wave:

1. Run `./scripts/spec-status.sh`.
2. Run `./scripts/check-specs.sh`.
3. Run `./scripts/validate.sh`.
4. Confirm all acceptance criteria in `spec.md` are satisfied.
5. Mark Spec 003 as `completed` in `specs/README.md`.
