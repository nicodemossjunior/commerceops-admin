# Spec: Coupons

## Objective

Implement promotional coupon management for CommerceOps Admin, including fixed and percentage discounts, validity period, usage limits, status, and validation rules.

## Scope

- Create, list, update, view, activate, deactivate, and soft delete coupons.
- Support fixed amount discounts.
- Support percentage discounts.
- Support validity windows.
- Support total usage limits.
- Support per-customer usage limit as a future-ready field.
- Track coupon status.
- Apply hybrid ID strategy.
- Apply soft delete by default.

## Out Of Scope

- Automatic coupon application.
- Checkout integration.
- Customer-facing coupon validation endpoint.
- Campaign management.
- Advanced segmentation rules.

## Coupon Status

```text
ACTIVE
INACTIVE
EXPIRED
```

## Discount Type

```text
FIXED_AMOUNT
PERCENTAGE
```

## Business Rules

- Coupon code must be unique among non-deleted coupons.
- Coupon code should be uppercase and trimmed.
- Fixed amount discount must be greater than zero.
- Percentage discount must be greater than zero and less than or equal to 100.
- Validity end date must be after start date when both are provided.
- Usage limit must be greater than zero when provided.
- Inactive coupons cannot be applied by future checkout flows.
- Expired coupons must not be considered active.

## Data Model

Table:

```text
coupon
```

`coupon` fields:

```text
id
public_id
code
description
discount_type
discount_value
starts_at
ends_at
usage_limit
usage_count
per_customer_limit
status
created_at
updated_at
deleted
deleted_at
deleted_by
```

## API Contracts

```text
GET    /api/coupons
POST   /api/coupons
GET    /api/coupons/{publicId}
PUT    /api/coupons/{publicId}
PATCH  /api/coupons/{publicId}/activate
PATCH  /api/coupons/{publicId}/deactivate
DELETE /api/coupons/{publicId}
```

Coupon filters:

```text
code
status
discountType
activeAt
page
size
sort
```

## Validation

- Code is required.
- Code must be unique among non-deleted coupons.
- Discount type is required.
- Discount value is required.
- Discount value must match the selected discount type rules.
- Status is required.
- Date range must be valid.
- Usage limits must be positive when provided.

## Authorization

- `ADMIN` can perform all coupon operations.
- `MANAGER` can create, update, activate, deactivate, and delete coupons.
- `READ_ONLY` can read coupons.
- `SUPPORT` can read coupons.
- `CATALOG` has no coupon write access.

## Expected Errors

- `VALIDATION_ERROR` for invalid payloads.
- `RESOURCE_NOT_FOUND` when coupon does not exist.
- `DUPLICATE_RESOURCE` for duplicate coupon code.
- `BUSINESS_RULE_VIOLATION` for invalid discount or date rules.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Coupons can be created, listed, updated, viewed, activated, deactivated, and soft deleted.
- Coupon code uniqueness is enforced.
- Fixed and percentage discount validations are enforced.
- Expired or inactive coupon state is represented clearly.
- Coupon API responses expose `publicId` and never internal IDs.

## Expected Tests

- Coupon CRUD tests.
- Duplicate code test.
- Discount validation tests.
- Date validation tests.
- Activation and deactivation tests.
- Soft delete tests.
- Authorization tests.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.

