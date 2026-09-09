# CommerceOps Admin Specifications

This directory contains the Specification Driven Development documentation for CommerceOps Admin.

The project must be developed in English across code, API contracts, validation messages, specs, tasks, commits, and documentation.

## Specification Index

| ID | Specification | Status | Purpose |
| --- | --- | --- | --- |
| 000 | [Project Foundation](000-project-foundation/spec.md) | completed | Spring Boot project setup, package structure, shared backend standards, and core dependencies. |
| 001 | [Local Environment](001-local-environment/spec.md) | completed | Local development setup with PostgreSQL, Docker Compose, profiles, and Flyway bootstrap. |
| 002 | [Authentication](002-auth/spec.md) | completed | JWT authentication, admin users, roles, password hashing, and protected endpoints. |
| 003 | [Catalog](003-catalog/spec.md) | completed | Categories, products, SKU, pricing, inventory basics, filtering, pagination, and soft delete. |
| 004 | [Customers](004-customers/spec.md) | completed | Customer records, contact data, status, internal notes, and purchase history access. |
| 005 | [Orders](005-orders/spec.md) | completed | Order listing, details, items, status transitions, history, cancellation, and refunds. |
| 006 | [Coupons](006-coupons/spec.md) | planned | Promotional coupons, discount rules, validity, usage limits, and activation status. |
| 007 | [Dashboard](007-dashboard/spec.md) | planned | Operational summary metrics for revenue, orders, customers, inventory, and cancellations. |
| 008 | [Audit](008-audit/spec.md) | planned | Audit trail for sensitive actions and relevant domain changes. |
| 009 | [Observability](009-observability/spec.md) | planned | Structured logs, request correlation, metrics, actuator exposure, and tracing readiness. |
| 010 | [API Documentation](010-api-documentation/spec.md) | planned | OpenAPI documentation, API examples, error documentation, and endpoint discoverability. |
| 011 | [CI Pipeline](011-ci-pipeline/spec.md) | planned | Build, test, integration test, static analysis, and future Docker image steps. |

## Global Decisions

- Build tool: Maven.
- Runtime: Java 21.
- Framework: Spring Boot.
- Authentication: JWT.
- Base package: `com.commerceops.admin`.
- ID strategy: hybrid ID model.
- Internal primary keys: `Long`.
- External resource identifiers: `UUID` through a `publicId` field.
- Soft delete: default for business entities where deletion is user-facing or audit-relevant.
- Database: PostgreSQL.
- Migration tool: Flyway.
- API language: English.
- Code and documentation language: English.

## ID Strategy

Business entities should use two identifiers:

- `id`: internal `Long` primary key, optimized for joins and database indexing.
- `publicId`: external `UUID`, used in API paths and future integrations.

API endpoints should expose `publicId` instead of internal `id`.

Example:

```text
GET /api/products/{publicId}
GET /api/customers/{publicId}
GET /api/orders/{publicId}
```

## Soft Delete Standard

Soft delete should be used by default for entities that represent business records.

Standard fields:

```text
deleted
deleted_at
deleted_by
```

Application-level fields:

```text
deleted
deletedAt
deletedBy
```

Default list queries must exclude deleted records. Administrative filters may later expose `includeDeleted=true` where there is a clear operational need.

## API Error Format

All API errors must follow a stable response shape.

```json
{
  "timestamp": "2026-08-12T16:21:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "path": "/api/products",
  "traceId": "8f3a1c0e4c9b4b2a",
  "fieldErrors": [
    {
      "field": "name",
      "message": "Product name is required."
    }
  ]
}
```

Standard error codes:

```text
VALIDATION_ERROR
RESOURCE_NOT_FOUND
DUPLICATE_RESOURCE
AUTHENTICATION_FAILED
ACCESS_DENIED
BUSINESS_RULE_VIOLATION
INVALID_REQUEST
INTERNAL_SERVER_ERROR
```

## Database Naming Conventions

- Use `snake_case` for tables, columns, indexes, constraints, and migration names.
- Use singular table names for domain entities.
- Avoid SQL reserved words. Use `sales_order` instead of `order`.
- Name foreign keys with the referenced entity plus `_id`.
- Name public UUID columns as `public_id`.

Recommended table names:

```text
admin_user
role
admin_user_role
category
product
customer
customer_note
sales_order
order_item
coupon
inventory_movement
audit_log
```

Standard columns:

```text
id
public_id
created_at
updated_at
deleted
deleted_at
deleted_by
created_by
updated_by
```

Constraint naming:

```text
pk_product
uk_product_public_id
uk_product_sku
fk_product_category
ck_product_status
```

Index naming:

```text
idx_product_public_id
idx_product_sku
idx_product_status
idx_product_category_id
idx_customer_public_id
idx_sales_order_public_id
idx_sales_order_customer_id
```

Flyway migration naming:

```text
V001__create_admin_users_and_roles.sql
V002__create_categories_table.sql
V003__create_products_table.sql
V004__create_customers_table.sql
V005__create_sales_orders_table.sql
```

## Spec File Standard

Each specification folder must contain:

```text
spec.md
tasks.md
```

New specifications should start from `TEMPLATE.md`.

Each `spec.md` should define:

- Objective.
- Scope.
- Out of scope.
- Business rules.
- Data model.
- API contracts.
- Validation.
- Authorization.
- Expected errors.
- Migration plan.
- Cross-spec impact.
- Implementation notes for agents.
- Non-negotiable constraints.
- Acceptance criteria.
- Expected tests.
- Done means.
- Dependencies.

Each `tasks.md` should define implementation tasks grouped by area:

- Backend.
- Database and Flyway.
- Tests.
- Documentation.
- Validation.
