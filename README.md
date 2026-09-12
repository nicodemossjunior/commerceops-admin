# CommerceOps Admin

CommerceOps Admin is a modular Spring Boot backend for e-commerce administration. The project is developed through Specification Driven Development, with each implementation step documented under `specs/`.

## Current Scope

The current implementation covers the project foundation, local environment, and authentication:

- Maven project using Java 21 and Maven Wrapper.
- Spring Boot application under the `com.commerceops.admin` base package.
- Modular package structure for future business capabilities.
- Shared API error response format.
- Shared pagination, validation, security, and persistence conventions.
- Local and test profile configuration.
- Core dependencies for Web, Validation, Security, JPA, PostgreSQL, Flyway, Actuator, OpenAPI, and Testcontainers.
- Docker Compose setup for local PostgreSQL.
- Flyway bootstrap migration and validation coverage.
- JWT login and authenticated user profile endpoints.
- Administrative users, roles, BCrypt password hashes, and role-based authorization support.

No business domain CRUD feature is implemented yet.

## Technology Stack

- Java 21.
- Spring Boot.
- Maven.
- Spring Web.
- Spring Validation.
- Spring Security.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- Spring Boot Actuator.
- Springdoc OpenAPI.
- Testcontainers.

## Project Structure

```text
src/main/java/com/commerceops/admin
├── CommerceOpsAdminApplication.java
├── audit
├── auth
├── catalog
├── common
│   ├── error
│   ├── pagination
│   ├── persistence
│   ├── security
│   └── validation
├── config
├── coupons
├── customers
├── dashboard
├── observability
├── orders
└── security
```

## API Conventions

- API contracts expose `publicId` UUID values instead of internal database IDs.
- JPA entities must not be exposed directly through REST responses.
- Request DTOs must use Bean Validation.
- List endpoints must use pagination.
- Business entities use soft delete by default when deletion is user-facing or audit-relevant.
- API errors follow the global response format documented in `specs/README.md`.

## Configuration

The default Spring profile is `local`.

Main configuration files:

- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/test/resources/application-test.yml`

Important environment variables:

- `SERVER_PORT`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_TTL_SECONDS`
- `APP_ENVIRONMENT`
- `ROOT_LOG_LEVEL`
- `APPLICATION_LOG_LEVEL`
- `CONSOLE_LOG_FORMAT`
- `OTEL_TRACING_ENABLED`
- `OTEL_TRACES_SAMPLER_PROBABILITY`
- `OTEL_EXPORTER_OTLP_ENDPOINT`
- `OTEL_EXPORTER_OTLP_PROTOCOL`

Copy `.env.example` to `.env` when you want to override local defaults.

## Local Development

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the backend with the local profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The local profile connects to PostgreSQL through:

```text
jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:commerceops_admin}
```

Flyway runs automatically on startup using migrations from:

```text
src/main/resources/db/migration
```

Reset the local database:

```bash
docker compose down -v
docker compose up -d postgres
```

Flyway migrations must use a three-digit sequence number, lowercase words separated by underscores, and a clear change description:

```text
V001__bootstrap_database.sql
V002__create_admin_users_and_roles.sql
V003__create_categories_table.sql
V004__create_products_table.sql
```

## Observability

Local Actuator endpoints, structured log fields, correlation headers, sensitive-data rules, application metrics, and the future OpenTelemetry path are documented in [`docs/observability.md`](docs/observability.md).

## Tests

Run the test suite with:

```bash
./mvnw test
```

The test suite currently includes:

- Application context load test.
- API error response serialization test.
- Flyway migration validation test.
- Authentication and authorization tests for login, JWT-protected endpoints, disabled or deleted users, invalid tokens, and insufficient roles.

## AI-Assisted Development Workflow

Repository-local agent instructions live in `AGENTS.md`. Agents and contributors should use the Maven Wrapper and the repository scripts instead of ad hoc commands:

```bash
./scripts/spec-status.sh
./scripts/check-specs.sh
./scripts/validate.sh
```

`./scripts/validate.sh` runs the spec structure check and the Maven test suite.

Optional Git hooks are versioned under `.githooks/`. Enable them with:

```bash
git config core.hooksPath .githooks
```

The hooks validate spec structure before commits, enforce English Conventional Commit messages, and run the full validation script before pushes.

New specifications should start from `specs/TEMPLATE.md`.

## Specification Flow

Implementation should follow the specifications in order unless a later decision changes the roadmap:

1. `000-project-foundation`
2. `001-local-environment`
3. `002-auth`
4. `003-catalog`
5. `004-customers`
6. `005-orders`
7. `006-coupons`
8. `007-dashboard`
9. `008-audit`
10. `009-observability`
11. `010-api-documentation`
12. `011-ci-pipeline`
