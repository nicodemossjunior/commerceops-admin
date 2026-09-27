# Tasks: Automated API Journeys

## Backend and Test Infrastructure

- [ ] Inventory supported operations, filters, and role permissions in a coverage matrix.
- [ ] Configure random-port Spring Boot HTTP tests and PostgreSQL Testcontainers with production Flyway migrations.
- [ ] Add isolated fixture setup, cleanup, deterministic time handling, and real JWT login helpers.
- [ ] Reuse suitable scenario definitions from spec 012 without using its persistent database or manifest.
- [ ] Add sanitized failure diagnostics and ensure required infrastructure failures do not skip tests.

## Database and Flyway

- [ ] Verify the suite uses only ephemeral PostgreSQL and production migration configuration.
- [ ] Verify data isolation across HTTP requests and independent journeys.
- [ ] Cover PostgreSQL uniqueness, references, and soft-delete behavior through API assertions.

## Tests

- [ ] Implement login, identity, invalid/expired token, and role permission journeys.
- [ ] Implement category and product registration, mutation, lookup, and deletion journeys.
- [ ] Implement customer, note, and purchase-history journeys.
- [ ] Implement coupon creation, update, activation, expiration, and deletion journeys.
- [ ] Implement order lookup, valid transitions, cancellation, refund, and history assertions.
- [ ] Cover supported filters individually and in combinations, pagination, and stable sorting.
- [ ] Cover invalid requests, duplicates, missing resources, forbidden actions, and unchanged state after rejection.
- [ ] Verify historical order snapshots survive referenced product edits and soft deletion.
- [ ] Verify audit actors, actions, target IDs, redaction, and available filters.
- [ ] Verify exact dashboard metrics and supporting lists before and after operations.
- [ ] Cover all period shortcuts, inclusive boundaries, invalid ranges, global counters, and empty data.

## CI and Documentation

- [ ] Integrate `*IT.java` discovery and execution with existing Failsafe and CI stages.
- [ ] Make integration test reports available in CI, including failed runs.
- [ ] Document Docker requirements, fast versus full validation, individual journey execution, and report locations.
- [ ] Complete the operation/filter/permission matrix with references to implemented tests.

## Validation

- [ ] Run `./scripts/spec-status.sh` and `./scripts/check-specs.sh`.
- [ ] Run `./scripts/validate.sh`.
- [ ] Run `./mvnw verify` with Docker available.
- [ ] Run `./scripts/ci.sh`.
- [ ] Run each journey independently and repeat the full integration suite to verify isolation.
