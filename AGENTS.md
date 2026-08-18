# CommerceOps Admin Agent Instructions

This repository is developed with Specification Driven Development. Agents must keep code, documentation, API messages, validation messages, specs, tasks, and commit messages in English.

## Required Workflow

1. Read `specs/README.md`.
2. Read the active spec `spec.md` and `tasks.md`.
3. Before implementing a requested spec, check the immediately previous spec.
4. If the previous spec is incomplete, finish it first, validate it, update its `tasks.md`, commit it, and only then start the requested spec.
5. Implement only what the active spec asks for.
6. Update `tasks.md` as tasks are completed.
7. Run `./scripts/validate.sh` before finishing implementation work.

## Project Standards

- Runtime: Java 21.
- Build tool: Maven through `./mvnw`.
- Base package: `com.commerceops.admin`.
- Database: PostgreSQL.
- Migrations: Flyway.
- Authentication: JWT.
- API docs: OpenAPI.
- Use DTOs for request and response contracts.
- Do not expose JPA entities directly through REST.
- Use Bean Validation for request DTOs.
- Use pagination for list endpoints.
- Expose `publicId` UUID values through APIs, not internal `Long` IDs.
- Apply soft delete by default for business entities.

## Validation Commands

Use these commands from the repository root:

```bash
./scripts/spec-status.sh
./scripts/check-specs.sh
./scripts/validate.sh
```

## Git Conventions

- Use Conventional Commits in English.
- Prefer focused commits aligned with one spec or one infrastructure change.
- Do not include unrelated workspace changes.
- Do not commit local secrets or `.env` files.

## Local Hooks

The repository includes versioned hooks under `.githooks/`. Enable them with:

```bash
git config core.hooksPath .githooks
```
