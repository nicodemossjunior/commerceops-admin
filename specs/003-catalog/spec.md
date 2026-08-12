# Spec: Catalog

## Objective

Implement catalog management for CommerceOps Admin, covering categories, products, product status, SKU, pricing, stock fields, filtering, pagination, and soft delete.

## Scope

- Manage categories and subcategories.
- Manage products.
- Support product SKU, name, description, price, images, stock quantity, and status.
- Support product filtering by category, status, price range, SKU, name, and stock state.
- Support paginated product and category listing.
- Apply hybrid ID strategy with internal `Long` IDs and external `publicId` UUIDs.
- Apply soft delete by default.
- Protect write operations with proper roles.

## Out Of Scope

- Advanced image upload/storage.
- Product variants.
- Multi-warehouse inventory.
- Supplier management.
- Public storefront APIs.
- Full inventory movement history, except fields required for product stock basics.

## Business Rules

- Product SKU must be unique among non-deleted products.
- Product price must be greater than or equal to zero.
- Product stock quantity must be greater than or equal to zero.
- Product status must be one of the supported values.
- Deleted categories and products must not appear in default listings.
- A category with active products should not be hard deleted.
- Product status may be manually managed by admin users.
- Products with zero stock may be marked as `OUT_OF_STOCK`.

## Product Status

```text
DRAFT
ACTIVE
INACTIVE
OUT_OF_STOCK
```

## Data Model

Tables:

```text
category
product
```

`category` fields:

```text
id
public_id
parent_id
name
slug
description
status
created_at
updated_at
deleted
deleted_at
deleted_by
```

`product` fields:

```text
id
public_id
category_id
sku
name
slug
description
price
image_url
stock_quantity
status
created_at
updated_at
deleted
deleted_at
deleted_by
```

## API Contracts

Category endpoints:

```text
GET    /api/categories
POST   /api/categories
GET    /api/categories/{publicId}
PUT    /api/categories/{publicId}
DELETE /api/categories/{publicId}
```

Product endpoints:

```text
GET    /api/products
POST   /api/products
GET    /api/products/{publicId}
PUT    /api/products/{publicId}
DELETE /api/products/{publicId}
```

Product filters:

```text
categoryId
status
sku
name
minPrice
maxPrice
lowStock
page
size
sort
```

## Validation

- Category name is required.
- Category slug must be unique among non-deleted categories.
- Product name is required.
- Product SKU is required.
- Product SKU must be unique among non-deleted products.
- Product price cannot be negative.
- Product stock quantity cannot be negative.
- Product category must exist and must not be deleted.
- Product status is required.

## Authorization

- `ADMIN` can perform all catalog operations.
- `MANAGER` can read and update catalog records.
- `CATALOG` can create, update, and delete catalog records.
- `READ_ONLY` can only read catalog records.
- `SUPPORT` can read catalog records when needed for order/customer support.

## Expected Errors

- `VALIDATION_ERROR` for invalid payloads.
- `RESOURCE_NOT_FOUND` when category or product does not exist.
- `DUPLICATE_RESOURCE` for duplicate SKU or slug.
- `BUSINESS_RULE_VIOLATION` when a category cannot be deleted due to active products.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Categories can be created, listed, updated, viewed, and soft deleted.
- Products can be created, listed, updated, viewed, and soft deleted.
- Product list supports filtering and pagination.
- Deleted records are excluded from default queries.
- API exposes `publicId` instead of internal database IDs.
- Catalog write operations are role-protected.

## Expected Tests

- Category CRUD controller tests.
- Product CRUD controller tests.
- Product validation tests.
- Duplicate SKU test.
- Soft delete behavior tests.
- Repository filtering tests.
- Role-based access tests.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.

