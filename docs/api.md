# API Documentation

The generated OpenAPI contract is available at `GET /v3/api-docs`. With the `local` profile, Swagger UI is available at `GET /swagger-ui/index.html`.

## Authentication

Call `POST /api/auth/login` with an administrator email and password. Send the returned token on protected requests:

```http
Authorization: Bearer <access-token>
```

```json
{
  "email": "admin@commerceops.example",
  "password": "strong-password"
}
```

The OpenAPI contract declares the JWT scheme as `bearerAuth`. The login operation is public; `GET /api/auth/me` accepts any authenticated, enabled administrative user.

## Resources and Authorization

API paths use resource `publicId` UUID values. Internal database IDs are never part of the public contract.

| Domain | Endpoints | Read roles | Write roles |
| --- | --- | --- | --- |
| Categories | `/api/categories` | ADMIN, MANAGER, CATALOG, READ_ONLY, SUPPORT | ADMIN, CATALOG; MANAGER may update |
| Products | `/api/products` | ADMIN, MANAGER, CATALOG, READ_ONLY, SUPPORT | ADMIN, CATALOG; MANAGER may update |
| Customers | `/api/customers` | ADMIN, MANAGER, SUPPORT, READ_ONLY, CATALOG | ADMIN, MANAGER |
| Customer notes | `/api/customers/{customerPublicId}/notes` | ADMIN, SUPPORT | ADMIN, SUPPORT |
| Orders | `/api/orders` | ADMIN, MANAGER, SUPPORT, READ_ONLY, CATALOG | ADMIN, MANAGER |
| Coupons | `/api/coupons` | ADMIN, MANAGER, SUPPORT, READ_ONLY, CATALOG | ADMIN, MANAGER |
| Dashboard | `/api/dashboard/summary` | ADMIN, MANAGER, READ_ONLY, SUPPORT | Not applicable |
| Audit logs | `/api/audit-logs` | ADMIN, MANAGER | Not applicable |

Each generated operation includes an `x-required-roles` extension and repeats its access requirement in the description.

## Pagination and Filtering

Paginated endpoints accept:

- `page`: zero-based page index; example `0`.
- `size`: resources per page; example `20`.
- `sort`: `property,(asc|desc)` and may be repeated; example `createdAt,desc`.

Paginated responses contain `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`.

Available filters are documented on their operations:

- Products: `categoryId`, `status`, `sku`, `name`, `minPrice`, `maxPrice`, `lowStock`.
- Customers: `name`, `email`, `phone`, `status`.
- Orders: `orderNumber`, `customerId`, `status`, `paymentStatus`, `deliveryStatus`, `createdFrom`, `createdTo`.
- Coupons: `code`, `status`, `discountType`, `activeAt`.
- Dashboard: `period`, `from`, `to`.
- Audit logs: `actorUserId`, `actorEmail`, `action`, `entityType`, `entityPublicId`, `createdFrom`, `createdTo`.

Dates and times use ISO 8601 UTC, for example `2026-08-12T16:21:00Z`. Date-range boundaries are inclusive where stated by the operation.

## Status Enumerations

- Category: `ACTIVE`, `INACTIVE`.
- Product: `DRAFT`, `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`.
- Customer: `ACTIVE`, `INACTIVE`, `BLOCKED`.
- Order: `PENDING`, `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUNDED`.
- Payment: `PENDING`, `PAID`, `FAILED`, `REFUNDED`.
- Delivery: `PENDING`, `PREPARING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.
- Coupon: `ACTIVE`, `INACTIVE`, `EXPIRED`.
- Discount type: `FIXED_AMOUNT`, `PERCENTAGE`.
- Dashboard period: `TODAY`, `LAST_7_DAYS`, `LAST_30_DAYS`, `CUSTOM`.

Audit action values are generated from the `AuditAction` enum in the OpenAPI schema.

## Standard Errors

All errors use `ApiErrorResponse`. Reusable OpenAPI response components document the standard cases:

| HTTP status | Code |
| --- | --- |
| 400 | `VALIDATION_ERROR` or `INVALID_REQUEST` |
| 401 | `AUTHENTICATION_FAILED` |
| 403 | `ACCESS_DENIED` |
| 404 | `RESOURCE_NOT_FOUND` |
| 409 | `DUPLICATE_RESOURCE` |
| 422 | `BUSINESS_RULE_VIOLATION` |
| 500 | `INTERNAL_SERVER_ERROR` |

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

The generated component schemas contain English request and response examples for every implemented domain.
