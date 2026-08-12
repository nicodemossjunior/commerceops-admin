# Spec: Project Foundation

## Objective

Create the backend foundation for CommerceOps Admin as a modular Spring Boot application prepared for a professional portfolio project and future feature delivery.

## Scope

- Create a Maven-based Spring Boot project.
- Use Java 21.
- Use the base package `com.commerceops.admin`.
- Define the initial modular package structure.
- Add core dependencies for Web, Validation, Security, JPA, PostgreSQL, Flyway, Actuator, OpenAPI, and Testcontainers.
- Establish global API conventions.
- Establish the standard API error response format.
- Establish global entity conventions for auditing, hybrid IDs, and soft delete.
- Configure application profiles for local, test, and production-like environments.

## Out Of Scope

- Domain CRUD implementations.
- JWT login flow.
- Docker Compose setup.
- CI pipeline.
- Frontend application.

## Technical Decisions

- Build tool: Maven.
- Base package: `com.commerceops.admin`.
- Internal primary keys: `Long`.
- External resource identifiers: `UUID` via `publicId`.
- API path IDs should use `publicId`.
- Business entities should use soft delete by default.
- API responses and errors must be written in English.

## Package Structure

```text
src/main/java/com/commerceops/admin
├── CommerceOpsAdminApplication.java
├── common
│   ├── error
│   ├── pagination
│   ├── persistence
│   ├── security
│   └── validation
├── config
├── security
├── catalog
├── customers
├── orders
├── coupons
├── dashboard
├── audit
└── observability
```

## API Error Format

All API errors must use the global error format defined in `specs/README.md`.

The initial implementation should include support for:

- Validation errors.
- Resource not found errors.
- Duplicate resource errors.
- Business rule violations.
- Authentication failures.
- Access denied responses.
- Internal server errors.

## Entity Standards

Business entities should include:

- `id`: internal `Long` primary key.
- `publicId`: external `UUID`.
- `createdAt`.
- `updatedAt`.
- `deleted`.
- `deletedAt`.
- `deletedBy`, when the actor is known.

## Acceptance Criteria

- The backend project can be built with Maven.
- The application starts successfully using the local profile once the local environment spec is implemented.
- The package structure follows the modular boundaries defined in this spec.
- Shared conventions for error handling, IDs, auditing, and soft delete are documented and ready for implementation.
- No domain feature is implemented as part of this foundation spec.

## Expected Tests

- Application context load test.
- Basic test to verify global error response serialization once the error handler exists.
- No integration tests are required until database configuration is available.

## Dependencies

- None.

