# Tasks: Audit

## Backend

- [x] Create `AuditLog` entity.
- [x] Create audit action enum.
- [ ] Create audit service.
- [x] Create audit repository.
- [ ] Create audit response DTOs.
- [ ] Create audit controller.
- [ ] Add audit filtering and pagination.
- [ ] Add helper for recording domain actions.
- [ ] Add metadata redaction utility.
- [ ] Add trace ID support when available.

## Database And Flyway

- [x] Create `audit_log` table migration.
- [x] Add unique constraint for audit log `public_id`.
- [x] Add indexes for `actor_user_id`, `actor_email`, `action`, `entity_type`, and `created_at`.

## Tests

- [ ] Test audit record creation.
- [ ] Test audit listing filters.
- [ ] Test audit detail lookup.
- [ ] Test secret redaction.
- [ ] Test role-based access.

## Documentation

- [ ] Document audit endpoints in OpenAPI.
- [ ] Document audited action values.
- [ ] Document metadata redaction rules.

## Validation

- [ ] Run unit tests.
- [ ] Run controller tests.
- [ ] Run repository integration tests.
- [ ] Run migration validation.
