# Spec: Automated API Journeys

---
id: 013
status: planned
depends_on: [012, 011]
last_updated: 2026-09-26
---

## Objective

Verify complete administrative journeys across registrations, order operations, filters, audit records, and dashboard calculations using the real HTTP API and an isolated PostgreSQL database.

## Scope

- Add Java integration tests named `*IT.java`, executed by Maven Failsafe.
- Start the Spring Boot application on a random port and PostgreSQL through Testcontainers, applying production Flyway migrations.
- Authenticate through the real login endpoint and use issued JWTs for subsequent HTTP requests.
- Exercise every supported write operation for categories, products, customers, customer notes, and coupons, followed by reads that verify persistence.
- Exercise order lifecycle operations and verify history, audit events, and dashboard changes.
- Cover supported filters individually and in representative combinations, pagination, sorting, soft delete, error contracts, and role permissions.
- Integrate the suite into the existing CI pipeline and document local execution and reports.

## Out Of Scope

- Frontend creation, browser tests, load testing, and external payment or shipping systems.
- New registration or order creation APIs and coupon redemption features.
- Replacing existing unit and MockMvc tests or changing business rules to make fixtures pass.
- Running tests against the persistent demo database or a developer's ordinary application database.

## Business Rules

- Each journey must establish its own prerequisites and run independently of test execution order.
- Reuse deterministic scenario definitions from spec 012 where helpful, with a smaller fixture for exact assertions; do not require its local manifest or demo command.
- Use HTTP for supported business operations under test. Direct setup is limited to prerequisites without an API, such as role accounts, initial orders, and historical timestamps.
- Order fixture preparation must maintain valid relationships, monetary totals, item snapshots, and lifecycle metadata.
- Authenticate actual role accounts; do not mock security, controllers, services, repositories, or the current user provider.
- Use only public UUIDs in requests and verify response contracts do not expose internal entity IDs.
- Assert returned records and values, not only HTTP status or aggregate counts.
- Expected results must come from independently specified fixture values, not the production query or service being tested.
- Use deterministic time control or explicitly anchored custom periods. Current-period shortcuts must be tested with a controlled application clock where needed, without sleeps or midnight-dependent assumptions.
- Isolate data between tests using fresh databases, schemas, or explicit cleanup. A transaction on the test method does not roll back writes made through the HTTP server.

## Data Model

No production schema changes are planned. Use PostgreSQL containers with existing Flyway migrations, constraints, indexes, and unique predicates. Test accounts and fixture data are ephemeral and belong only to the test environment.

## API Contracts

Use the current OpenAPI document and controllers as the inventory of supported operations. Build a traceable matrix mapping each scoped operation and filter to its journey assertions.

Required journeys:

1. Login and identity: valid and invalid credentials, authenticated identity, missing/invalid/expired tokens, and denied roles.
2. Catalog: category and product creation, reads, updates, supported status changes, filters, pagination, sorting, validation, duplicate conflicts, and soft delete.
3. Customers: registration, edits, supported status changes, notes and their supported mutations, filters, purchase history, and soft delete.
4. Coupons: registration, edits, activation/deactivation, validity representation, supported filters, duplicate and validation errors, and soft delete.
5. Orders: list and details, customer history, valid fulfillment transitions, cancellation, refund, invalid transitions, and required reasons.
6. Audit: verify supported sensitive operations produce expected actor, action, target public ID, and sanitized metadata; exercise available audit filters and pagination.
7. Dashboard: verify exact metrics and supporting lists before and after operations, all supported period shortcuts, custom periods, invalid ranges, and empty results.

There is no `POST /api/orders`; initial orders are test fixtures. Do not assume coupon redemption or order-driven inventory changes exist.

## Validation

- Cover required fields, invalid enum values, malformed UUIDs, missing resources, duplicate values, and existing business-rule violations.
- Verify the applicable HTTP status, stable error code, error shape, and field errors according to current contracts.
- Verify inclusive time boundaries and exclusion of adjacent records, using timestamps representable at PostgreSQL precision.
- Verify pagination metadata and explicit deterministic sorting, including multiple pages without missing or repeated records.
- Verify deleted records are excluded from normal lists while historical order items remain readable.

## Authorization

- Cover ADMIN, MANAGER, SUPPORT, CATALOG, and READ_ONLY using real JWTs and the permissions defined by the existing controllers.
- Include permitted and forbidden reads and writes for each role where applicable.
- Provision test accounts with the existing password encoder and ephemeral test credentials; do not log passwords or JWTs.

## Expected Errors

Assert existing contracts for validation, authentication, access denial, missing resources, duplicate resources, and invalid business transitions. Do not impose a new global error mapping in this spec. Docker or database startup failures must fail the integration stage with an actionable message rather than silently skipping tests.

## Migration Plan

Run existing production migrations inside PostgreSQL containers. Do not use H2 substitutions for these journeys or add test data to production migrations.

## Cross-Spec Impact

- Extend spec 011's existing Failsafe and CI stages; preserve the fast existing test command.
- `./scripts/validate.sh` currently runs spec checks and `./mvnw test`; it is necessary but not sufficient to execute `*IT.java` tests. Document `./mvnw verify` and `./scripts/ci.sh` for full validation.
- Reuse spec 012 fixture definitions without coupling integration tests to its persistent storage or reset lifecycle.
- Preserve spec 007 semantics: gross revenue excludes cancelled orders, net excludes cancelled and refunded orders, and average order value uses the remaining order count. Other statuses are included in these sums.
- Period filters apply to order creation dates and related metrics; customer and low-stock counters remain global. Recent orders and low-stock lists currently return at most five entries.
- Do not redefine these formulas as payment settlement or historical inventory accounting.

## Implementation Notes For Agents

- Read `AGENTS.md`, this spec and tasks, and complete, validate, and commit spec 012 before implementing this spec.
- Use Java 21, existing Maven dependencies, a real HTTP client, and Testcontainers.
- Keep fixtures small enough for exact assertions and routine CI use.
- Capture useful request paths, operation names, and expected/actual results on failure without credentials or sensitive payloads.
- Update tasks as work progresses and run both standard validation and the integration phase.

## Non-Negotiable Constraints

- Keep code, docs, messages, specs, tasks, and commits in English.
- Do not add frontend code, public endpoints, or dependencies on external services beyond Docker for PostgreSQL.
- Never point automated journeys at persistent demo, ordinary local, or production databases.
- Preserve existing DTO, public UUID, soft-delete, and error contracts.

## Acceptance Criteria

- A documented command executes all journeys against an ephemeral PostgreSQL container with real JWT authentication.
- The coverage matrix includes all supported writes and filters in scope, with explicit assertions and references to tests.
- Journeys pass independently and on repeated execution without external state or ordering dependencies.
- Exact dashboard results reflect order transitions, custom periods, global counters, low stock, and empty datasets.
- Role failures, validation failures, and rejected transitions leave business state unchanged where required.
- Audit checks verify the actual persisted events through the API, including redaction.
- CI executes the suite and fails on assertions or unavailable required infrastructure; reports are available for diagnosis.

## Expected Tests

- Complete HTTP journeys for authentication, catalog, customers/notes, coupons, orders, audit, and dashboard.
- Parameterized filter, role, invalid-input, and period cases where they improve traceability.
- PostgreSQL-specific constraint and soft-delete behavior exercised through HTTP.
- Repeatability checks and independent journey execution during validation.

## Done Means

- All tasks are complete, the operation/filter matrix and execution guide are documented, and CI runs the suite.
- `./scripts/validate.sh`, `./mvnw verify`, and `./scripts/ci.sh` pass with Docker available.
- The implementation is committed with an English Conventional Commit message.

## Dependencies

- `012-demo-scenarios` for deterministic scenario conventions.
- `011-ci-pipeline` for Maven Failsafe and CI integration.
- Specs 002 through 010 for domain, security, audit, and API contracts.
