# Tasks: Automated API Journeys

## Backend and Test Infrastructure

- [x] Inventory supported operations, filters, and role permissions in a coverage matrix.
- [x] Configure random-port Spring Boot HTTP tests and PostgreSQL Testcontainers with production Flyway migrations.
- [x] Add isolated fixture setup, cleanup, deterministic time handling, and real JWT login helpers.
- [x] Reuse suitable scenario definitions from spec 012 without using its persistent database or manifest.
- [x] Add sanitized failure diagnostics and ensure required infrastructure failures do not skip tests.

## Database and Flyway

- [x] Verify the suite uses only ephemeral PostgreSQL and production migration configuration.
- [x] Verify data isolation across HTTP requests and independent journeys.
- [x] Cover PostgreSQL uniqueness, references, and soft-delete behavior through API assertions.

## Tests

- [x] Implement login, identity, invalid/expired token, and role permission journeys.
- [x] Implement category and product registration, mutation, lookup, and deletion journeys.
- [x] Implement customer, note, and purchase-history journeys.
- [x] Implement coupon creation, update, activation, expiration, and deletion journeys.
- [x] Implement order lookup, valid transitions, cancellation, refund, and history assertions.
- [x] Cover supported filters individually and in combinations, pagination, and stable sorting.
- [x] Cover invalid requests, duplicates, missing resources, forbidden actions, and unchanged state after rejection.
- [x] Verify historical order snapshots survive referenced product edits and soft deletion.
- [x] Verify audit actors, actions, target IDs, redaction, and available filters.
- [x] Verify exact dashboard metrics and supporting lists before and after operations.
- [x] Cover all period shortcuts, inclusive boundaries, invalid ranges, global counters, and empty data.

## CI and Documentation

- [x] Integrate `*IT.java` discovery and execution with existing Failsafe and CI stages.
- [x] Make integration test reports available in CI, including failed runs.
- [x] Document Docker requirements, fast versus full validation, individual journey execution, and report locations.
- [x] Complete the operation/filter/permission matrix with references to implemented tests.

## Validation

- [x] Run `./scripts/spec-status.sh` and `./scripts/check-specs.sh`.
- [x] Run `./scripts/validate.sh`.
- [x] Run `./mvnw verify` with Docker available.
- [x] Run `./scripts/ci.sh`.
- [x] Run each journey independently and repeat the full integration suite to verify isolation.
