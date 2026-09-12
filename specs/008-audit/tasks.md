# Tasks: Audit

## Backend

- [x] Create `AuditLog` entity.
- [x] Create audit action enum.
- [x] Create audit service.
- [x] Create audit repository.
- [x] Create audit response DTOs.
- [x] Create audit controller.
- [x] Add audit filtering and pagination.
- [x] Add helper for recording domain actions.
- [x] Add metadata redaction utility.
- [x] Add trace ID support when available.

## Database And Flyway

- [x] Create `audit_log` table migration.
- [x] Add unique constraint for audit log `public_id`.
- [x] Add indexes for `actor_user_id`, `actor_email`, `action`, `entity_type`, and `created_at`.

## Tests

- [x] Test audit record creation.
- [x] Test audit listing filters.
- [x] Test audit detail lookup.
- [x] Test secret redaction.
- [x] Test role-based access.

## Documentation

- [x] Document audit endpoints in OpenAPI.
- [x] Document audited action values.
- [x] Document metadata redaction rules.

## Validation

- [x] Run unit tests.
- [x] Run controller tests.
- [x] Run repository integration tests.
- [x] Run migration validation.
