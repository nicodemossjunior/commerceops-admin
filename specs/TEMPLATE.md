# Spec: Feature Name

---
id: 000
status: planned
depends_on: []
last_updated: YYYY-MM-DD
---

## Objective

Describe the user-facing and technical goal of this specification.

## Scope

- List what must be implemented.
- Keep this section concrete and bounded.

## Out Of Scope

- List related work that must not be implemented in this spec.

## Business Rules

- Describe invariant business behavior.
- Include cross-entity rules and lifecycle rules.

## Data Model

Tables:

```text
table_name
```

Fields:

```text
id
public_id
created_at
updated_at
deleted
deleted_at
deleted_by
```

## API Contracts

```text
GET    /api/resources
POST   /api/resources
GET    /api/resources/{publicId}
PUT    /api/resources/{publicId}
DELETE /api/resources/{publicId}
```

## Validation

- Define request validation rules.
- Define uniqueness and reference validation.

## Authorization

- Define which roles can read.
- Define which roles can write.

## Expected Errors

- `VALIDATION_ERROR` for invalid payloads.
- `RESOURCE_NOT_FOUND` when a resource does not exist.
- `DUPLICATE_RESOURCE` for unique constraint conflicts.
- `ACCESS_DENIED` for insufficient role.

## Migration Plan

- Add expected Flyway migration names.
- Define indexes, constraints, and seed data.

## Cross-Spec Impact

- List specs or shared modules affected by this work.

## Implementation Notes For Agents

- Read `AGENTS.md` before implementation.
- Use DTOs for API contracts.
- Do not expose JPA entities through REST.
- Update this spec's `tasks.md` as tasks are completed.
- Run `./scripts/validate.sh` before finishing.

## Non-Negotiable Constraints

- Keep code, docs, API messages, validation messages, specs, tasks, and commits in English.
- Keep implementation scoped to this spec.
- Preserve the global API error format from `specs/README.md`.

## Acceptance Criteria

- Define observable completion criteria.
- Include endpoint behavior, persistence behavior, and authorization behavior.

## Expected Tests

- Define unit tests.
- Define controller tests.
- Define repository or migration tests where database behavior matters.

## Done Means

- All required tasks are checked in `tasks.md`.
- Tests pass through `./scripts/validate.sh`.
- Documentation has been updated.
- The implementation is committed with an English Conventional Commit message.

## Dependencies

- `000-project-foundation`.
