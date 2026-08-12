# Spec: Customers

## Objective

Implement customer management for CommerceOps Admin, including customer registration, search, status, contact data, internal notes, and access to purchase history derived from orders.

## Scope

- Create and manage customer records.
- Store contact data.
- Track customer status.
- Support internal notes for support and operations.
- Support customer search and pagination.
- Expose purchase history when order data exists.
- Apply hybrid ID strategy.
- Apply soft delete by default.

## Out Of Scope

- Customer-facing authentication.
- Customer self-service profile updates.
- Marketing segmentation.
- LGPD/GDPR automation workflows.
- Advanced consent management.

## Business Rules

- Customer email must be unique among non-deleted customers when provided.
- Customer status must be one of the supported values.
- Deleted customers must not appear in default listings.
- Internal notes are visible only to authorized administrative roles.
- Purchase history is read-only in this module and derived from orders.

## Customer Status

```text
ACTIVE
INACTIVE
BLOCKED
```

## Data Model

Tables:

```text
customer
customer_note
```

`customer` fields:

```text
id
public_id
name
email
phone
document
status
created_at
updated_at
deleted
deleted_at
deleted_by
```

`customer_note` fields:

```text
id
public_id
customer_id
note
created_by
created_at
updated_at
deleted
deleted_at
deleted_by
```

## API Contracts

Customer endpoints:

```text
GET    /api/customers
POST   /api/customers
GET    /api/customers/{publicId}
PUT    /api/customers/{publicId}
DELETE /api/customers/{publicId}
```

Customer note endpoints:

```text
GET    /api/customers/{publicId}/notes
POST   /api/customers/{publicId}/notes
DELETE /api/customers/{publicId}/notes/{notePublicId}
```

Purchase history endpoint:

```text
GET /api/customers/{publicId}/orders
```

Customer filters:

```text
name
email
phone
status
page
size
sort
```

## Validation

- Customer name is required.
- Email must be valid when provided.
- Phone must follow the accepted format when provided.
- Status is required.
- Internal note text is required.
- Customer must exist and must not be deleted when adding notes.

## Authorization

- `ADMIN` can perform all customer operations.
- `MANAGER` can create, update, read, and delete customer records.
- `SUPPORT` can read customers and manage internal notes.
- `READ_ONLY` can read customer records.
- `CATALOG` has no customer write access.

## Expected Errors

- `VALIDATION_ERROR` for invalid payloads.
- `RESOURCE_NOT_FOUND` when customer or note does not exist.
- `DUPLICATE_RESOURCE` for duplicate email.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Customers can be created, listed, updated, viewed, and soft deleted.
- Customer notes can be added, listed, and soft deleted.
- Customer list supports search, filters, and pagination.
- Deleted customers and notes are excluded by default.
- Customer order history endpoint is defined and can be implemented once orders exist.

## Expected Tests

- Customer CRUD tests.
- Customer validation tests.
- Duplicate email test.
- Customer soft delete tests.
- Customer notes tests.
- Authorization tests.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.
- `005-orders` for purchase history data.

