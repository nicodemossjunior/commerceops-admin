# CommerceOps Admin

CommerceOps Admin is a modular Spring Boot backend for e-commerce administration. The project is developed through Specification Driven Development, with each implementation step documented under `specs/`.

## Current Scope

The current implementation covers the project foundation only:

- Maven project using Java 21.
- Spring Boot application under the `com.commerceops.admin` base package.
- Modular package structure for future business capabilities.
- Shared API error response format.
- Shared pagination, validation, security, and persistence conventions.
- Local and test profile configuration.
- Core dependencies for Web, Validation, Security, JPA, PostgreSQL, Flyway, Actuator, OpenAPI, and Testcontainers.

No domain CRUD feature is implemented in this foundation step.

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

## Tests

Run the test suite with:

```bash
mvn test
```

The foundation currently includes:

- Application context load test.
- API error response serialization test.

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
