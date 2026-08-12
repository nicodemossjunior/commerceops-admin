# Spec: Audit

## Objective

Implement an audit trail for sensitive administrative actions and relevant domain changes in CommerceOps Admin.

## Scope

- Record sensitive actions performed by authenticated admin users.
- Store the actor, action, target entity, target identifier, timestamp, and optional metadata.
- Support audit listing and filtering.
- Support correlation with request trace IDs when available.
- Keep audit records immutable from the API perspective.

## Out Of Scope

- Full event sourcing.
- Audit record editing.
- External SIEM integration.
- Long-term archival policies.
- Advanced diff visualization.

## Audited Actions

Initial audited actions:

```text
AUTH_LOGIN_SUCCESS
AUTH_LOGIN_FAILURE
ADMIN_USER_CREATED
ADMIN_USER_UPDATED
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_DELETED
CATEGORY_CREATED
CATEGORY_UPDATED
CATEGORY_DELETED
CUSTOMER_CREATED
CUSTOMER_UPDATED
CUSTOMER_DELETED
ORDER_STATUS_CHANGED
ORDER_CANCELLED
ORDER_REFUNDED
COUPON_CREATED
COUPON_UPDATED
COUPON_DELETED
COUPON_ACTIVATED
COUPON_DEACTIVATED
```

## Data Model

Table:

```text
audit_log
```

Fields:

```text
id
public_id
actor_user_id
actor_email
action
entity_type
entity_public_id
trace_id
request_method
request_path
metadata_json
created_at
```

## API Contracts

```text
GET /api/audit-logs
GET /api/audit-logs/{publicId}
```

Filters:

```text
actorUserId
actorEmail
action
entityType
entityPublicId
createdFrom
createdTo
page
size
sort
```

## Business Rules

- Audit records must not be updated or deleted through the API.
- Audit logging failures must not break core business operations unless explicitly configured otherwise.
- Sensitive metadata must be redacted before persistence.
- Passwords, password hashes, JWTs, and secrets must never be stored in audit metadata.

## Authorization

- `ADMIN` can read audit logs.
- `MANAGER` may read operational audit logs if allowed by policy.
- Other roles cannot read audit logs by default.

## Expected Errors

- `VALIDATION_ERROR` for invalid filters.
- `RESOURCE_NOT_FOUND` when audit log does not exist.
- `ACCESS_DENIED` for insufficient role.

## Acceptance Criteria

- Sensitive domain actions generate audit records.
- Audit list supports filtering and pagination.
- Audit details can be retrieved by `publicId`.
- Audit metadata does not include secrets.
- Audit records are immutable from the API perspective.

## Expected Tests

- Audit record creation tests.
- Secret redaction tests.
- Audit listing filter tests.
- Audit authorization tests.
- Domain action integration tests where practical.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.
- `002-auth`.

