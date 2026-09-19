# CommerceOps Admin

[![Backend CI](https://github.com/nicodemossjunior/commerceops-admin/actions/workflows/ci.yml/badge.svg)](https://github.com/nicodemossjunior/commerceops-admin/actions/workflows/ci.yml)

CommerceOps Admin is a production-minded Spring Boot backend for administrative e-commerce operations. It combines catalog, customer, order, coupon, dashboard, and audit capabilities in a modular monolith secured with JWT and role-based authorization.

The project was built with Specification Driven Development. All planned specifications from foundation through CI are implemented and traceable under [`specs/`](specs/).

## Features

| Area | Capabilities |
| --- | --- |
| Authentication | JWT login, current-user profile, BCrypt password verification, stateless security, and five administrative roles |
| Catalog | Category and product CRUD, filters, pagination, inventory status, and soft delete |
| Customers | Customer CRUD, status management, internal notes, search, and purchase history |
| Orders | Paginated search, item snapshots, lifecycle transitions, cancellation, refunds, and immutable status history |
| Coupons | Fixed and percentage discounts, validity windows, usage limits, lifecycle actions, and eligibility filtering |
| Dashboard | Revenue, order, customer, cancellation, refund, recent-order, and low-stock indicators |
| Audit | Immutable records for authentication, sensitive actions, and domain changes with redacted metadata |
| Observability | Structured JSON logs, request and trace correlation, Actuator, Prometheus metrics, and OpenTelemetry readiness |
| API documentation | OpenAPI contract, Swagger UI, JWT scheme, examples, pagination, filters, roles, and reusable errors |
| Delivery quality | Maven Wrapper, Flyway validation, Checkstyle, automated tests, local Git hooks, and GitHub Actions CI |

## Engineering Highlights

- Java 21 and Spring Boot 3.5.
- Modular package structure organized by business capability.
- DTO-only REST contracts; JPA entities are never exposed directly.
- External UUID `publicId` values separated from internal database IDs.
- PostgreSQL schema managed exclusively through versioned Flyway migrations.
- Soft delete for user-facing business records.
- Stable API error shape with field validation details and trace correlation.
- Method-level role authorization for `ADMIN`, `MANAGER`, `SUPPORT`, `CATALOG`, and `READ_ONLY`.
- Low-cardinality business metrics and sensitive-data logging safeguards.
- CI stages for compilation, unit tests, integration-test discovery, packaging, static analysis, and artifact upload.

## Technology Stack

| Category | Technology |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot, Spring Web, Spring Validation |
| Security | Spring Security, JWT, BCrypt |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Database lifecycle | Flyway |
| API contract | Springdoc OpenAPI and Swagger UI |
| Observability | Actuator, Micrometer, Prometheus, structured JSON logging |
| Testing | JUnit 5, Spring Boot Test, MockMvc, H2, Testcontainers readiness |
| Build and quality | Maven Wrapper, Checkstyle, GitHub Actions |

## Architecture

The application is a modular monolith. Each domain owns its controllers, DTOs, models, repositories, and services while shared infrastructure remains under `common`, `config`, `security`, and `observability`.

```text
src/main/java/com/commerceops/admin
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

## Quick Start

### Prerequisites

- Java 21.
- Docker Engine or another Docker-compatible runtime.
- Docker Compose v2.
- Git.

The Maven Wrapper is included, so a system-wide Maven installation is not required.

### 1. Configure the local environment

From the repository root:

```bash
cp .env.example .env
```

Update `JWT_SECRET` in `.env`. Docker Compose reads this file automatically. To make the same variables available to Spring Boot, export them in the shell used to start the application:

```bash
set -a
source .env
set +a
```

Do not commit `.env`; it is intentionally ignored by Git.

### 2. Start PostgreSQL

```bash
docker compose up -d postgres
docker compose ps
```

PostgreSQL is exposed on `localhost:5432` by default. Flyway applies all migrations when the application starts.

### 3. Run the backend

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The default profile is already `local`; the explicit profile makes the intended environment clear.

### 4. Verify the application

| Resource | Local URL |
| --- | --- |
| Health check | <http://localhost:8080/actuator/health> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| Prometheus metrics | <http://localhost:8080/actuator/prometheus> |

`health` and `info` are public. Business endpoints, metrics, and Prometheus require authentication.

## Local Administrator Bootstrap

The project intentionally does not ship with a default administrator or shared password. The roles are created by Flyway, but an administrator must be provisioned before protected endpoints can be used.

For local development only, the following command creates `admin@example.com` with password `change-me-now` and assigns the `ADMIN` role:

```bash
docker compose exec -T postgres \
  psql -U "${DB_USERNAME:-commerceops}" -d "${DB_NAME:-commerceops_admin}" <<'SQL'
INSERT INTO admin_user (
    public_id,
    name,
    email,
    password_hash,
    status,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'Local Administrator',
    'admin@example.com',
    '$2a$10$2sJoRrlgoFtIZiLDT7G7Quyrdl7lQVnBVsF/UwJ8X1XoKLCOn0FWu',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO admin_user_role (admin_user_id, role_id)
SELECT admin_user.id, role.id
FROM admin_user
CROSS JOIN role
WHERE admin_user.email = 'admin@example.com'
  AND role.name = 'ADMIN';
SQL
```

This credential is documented for local bootstrap only. Use an independently generated BCrypt hash and a controlled provisioning process in any shared environment.

Authenticate with:

```bash
curl --request POST http://localhost:8080/api/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"email":"admin@example.com","password":"change-me-now"}'
```

Send the returned token on protected requests:

```http
Authorization: Bearer <access-token>
```

## API Overview

| Domain | Base path | Main operations |
| --- | --- | --- |
| Authentication | `/api/auth` | Login and current-user profile |
| Categories | `/api/categories` | List, create, retrieve, update, and soft delete |
| Products | `/api/products` | List with filters, create, retrieve, update, and soft delete |
| Customers | `/api/customers` | CRUD, purchase history, and internal notes |
| Orders | `/api/orders` | Search, details, status transitions, cancellation, and refunds |
| Coupons | `/api/coupons` | CRUD, activation, deactivation, and eligibility filters |
| Dashboard | `/api/dashboard/summary` | Period-based operational summary |
| Audit logs | `/api/audit-logs` | Filtered immutable log listing and detail |

List endpoints use zero-based `page`, `size`, and `sort` parameters. Date and time values use ISO 8601 UTC. Complete request schemas, response examples, filters, enums, and role requirements are available in Swagger UI and [`docs/api.md`](docs/api.md).

## Configuration

The application uses safe local defaults, except that `JWT_SECRET` should always be overridden. Important environment variables are:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `commerceops_admin` | Database name |
| `DB_USERNAME` | `commerceops` | Database user |
| `DB_PASSWORD` | `commerceops` | Local database password |
| `JWT_ISSUER` | `commerceops-admin` | JWT issuer |
| `JWT_SECRET` | Local-only fallback | JWT signing secret |
| `JWT_ACCESS_TOKEN_TTL_SECONDS` | `3600` | Access-token lifetime |
| `DASHBOARD_LOW_STOCK_THRESHOLD` | `10` | Low-stock threshold |
| `APP_ENVIRONMENT` | `local` | Log and metric environment tag |
| `ROOT_LOG_LEVEL` | `INFO` | Root log level |
| `APPLICATION_LOG_LEVEL` | `INFO` | Application log level |
| `CONSOLE_LOG_FORMAT` | `logstash` | Structured console format |
| `OTEL_TRACING_ENABLED` | `false` | Future tracing switch |
| `OTEL_TRACES_SAMPLER_PROBABILITY` | `0.10` | Future trace sampling ratio |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | Future OTLP endpoint |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `http/protobuf` | Future OTLP protocol |

Configuration files:

- [`application.yml`](src/main/resources/application.yml): shared defaults and production-safe Actuator exposure.
- [`application-local.yml`](src/main/resources/application-local.yml): PostgreSQL, Flyway, local Swagger UI, and local Actuator exposure.
- [`application-test.yml`](src/test/resources/application-test.yml): isolated H2 test database with PostgreSQL compatibility mode.

## Database Lifecycle

Flyway migrations live in [`src/main/resources/db/migration`](src/main/resources/db/migration) and are applied automatically. Hibernate validates the resulting schema and does not generate it.

Reset the local database and reapply every migration:

```bash
docker compose down -v
docker compose up -d postgres
```

The reset command permanently deletes local PostgreSQL data stored in the Compose volume.

## Testing and Quality

Run the test suite:

```bash
./mvnw test
```

Run specification validation and the complete test suite:

```bash
./scripts/validate.sh
```

Run the same compile, test, package, and static-analysis sequence used by CI:

```bash
./scripts/ci.sh
```

Run Checkstyle independently:

```bash
./mvnw checkstyle:check
```

The tests cover application startup, Flyway migrations, repositories, business services, controllers, authorization, OpenAPI generation, audit recording, observability, and error contracts. Tests use the isolated `test` profile; future `*IT.java` tests are reserved for PostgreSQL Testcontainers through Maven Failsafe.

## Continuous Integration

The [`Backend CI`](.github/workflows/ci.yml) workflow runs for pull requests and pushes to `main`. It compiles the project, runs tests, verifies Docker availability, packages the executable JAR, runs Checkstyle, and uploads the JAR as a short-lived artifact.

No application or infrastructure secrets are required for normal CI validation. Docker image publishing and deployment are intentionally disabled. See [`docs/ci.md`](docs/ci.md) for the complete pipeline contract.

## Observability

Every response receives `X-Request-Id` and `X-Trace-Id` headers. Request completion logs include correlation, route, status, duration, and authenticated user context without logging request bodies, authorization headers, passwords, or JWTs.

Local Actuator exposes health, info, metrics, and Prometheus endpoints. Domain meters cover orders, stock, authentication, and coupons. OpenTelemetry properties are reserved, but no collector or exporter is enabled yet.

See [`docs/observability.md`](docs/observability.md) for fields, metrics, security rules, and the future tracing path.

## API Standards

- Public contracts expose UUID `publicId` values, never internal `Long` IDs.
- Request and response DTOs isolate persistence models from the HTTP contract.
- Bean Validation produces a stable global error response.
- Pagination is mandatory for collection endpoints.
- Business records use soft delete where deletion is user-facing or audit-relevant.
- All API messages, validation messages, documentation, and source code are written in English.

## Specification Driven Development

The specification index in [`specs/README.md`](specs/README.md) records the purpose and completion status of every increment. Each spec contains acceptance criteria and an auditable task checklist. All specifications `000` through `011` are complete.

Repository automation:

```bash
./scripts/spec-status.sh
./scripts/check-specs.sh
```

Optional versioned Git hooks enforce spec structure, Conventional Commits, and pre-push validation:

```bash
git config core.hooksPath .githooks
```

## Documentation

- [API contract and authorization](docs/api.md)
- [Observability](docs/observability.md)
- [Continuous integration](docs/ci.md)
- [Specification index](specs/README.md)
- [Agent workflow](AGENTS.md)

## Current Boundaries

- This repository contains the backend only; there is no administrative frontend.
- Orders can be queried and operationally managed, but order creation and checkout belong to a separate commerce-facing system.
- Administrator self-registration and production identity provisioning are intentionally out of scope.
- OpenTelemetry export, Docker image publication, deployment, alerting, and external dashboards are prepared for future work but are not enabled.
