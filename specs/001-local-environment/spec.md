# Spec: Local Environment

## Objective

Provide a reliable local development environment for the backend using PostgreSQL, Docker Compose, Spring profiles, and Flyway migrations.

## Scope

- Define local PostgreSQL service with Docker Compose.
- Configure local application database access.
- Configure Flyway for migration execution.
- Create the initial database bootstrap migration when the backend exists.
- Establish naming standards for migrations, schemas, tables, indexes, and constraints.
- Support repeatable local development setup.

## Out Of Scope

- Production deployment.
- CI execution.
- Domain table creation beyond migrations required by each domain spec.
- Frontend local environment.

## Database

- Database engine: PostgreSQL.
- Schema: default `public`, unless a later decision introduces a dedicated schema.
- Migrations: Flyway.
- SQL naming: `snake_case`.
- Table naming: singular domain names.

## Migration Naming

Flyway migrations must use:

```text
V001__create_admin_users_and_roles.sql
V002__create_categories_table.sql
V003__create_products_table.sql
```

Guidelines:

- Use a three-digit sequence number.
- Use lowercase words separated by underscores.
- Describe the change clearly.
- Keep migrations focused by domain or table group.
- Avoid editing already-applied versioned migrations.

## Local Services

Initial local Docker Compose services:

- `postgres`.

Optional later services:

- `backend`.
- Observability tools.

## Acceptance Criteria

- PostgreSQL starts locally through Docker Compose.
- The backend can connect to PostgreSQL using the local profile.
- Flyway runs successfully on application startup.
- Migration naming conventions are documented and followed.
- Local setup instructions are clear enough for a portfolio reviewer to run the project.

## Expected Tests

- Repository integration tests should use Testcontainers when domain repositories are created.
- Flyway validation should pass in local and test profiles.

## Dependencies

- `000-project-foundation`.

