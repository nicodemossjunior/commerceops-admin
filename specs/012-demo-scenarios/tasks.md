# Tasks: Persistent Demo Scenarios

## Backend

- [x] Define versioned scenario inputs, ownership tracking, and manifest format.
- [x] Implement deterministic generators for all scoped entities and historical dates.
- [x] Implement coherent order lifecycle histories and historical item snapshots.
- [x] Implement explicit load, inspect, and reset commands.
- [x] Require demo configuration and validate the dedicated database identity before writes.
- [x] Implement idempotent loading, conflict detection, atomic failure handling, and ownership-scoped reset.
- [x] Generate independent expected metrics and representative filter examples.

## Database and Flyway

- [x] Configure a dedicated local demo PostgreSQL database using existing migrations.
- [x] Keep demo data out of ordinary Flyway migrations and normal startup.
- [x] Verify foreign keys, uniqueness, soft delete, and cleanup dependency order.
- [x] Ignore generated manifests and local credentials in version control where needed.

## Tests

- [x] Test deterministic inputs, monetary totals, relationships, and time boundaries.
- [x] Test PostgreSQL load, repeat load, reset, and rebuild.
- [x] Test unsafe targets, ownership conflicts, unrelated record preservation, and transaction failure recovery.
- [x] Verify representative API filters, historical snapshots, and dashboard expectations.
- [x] Verify ordinary application startup does not generate demo data.

## Documentation

- [x] Document database setup, credentials, commands, dataset composition, and reset behavior.
- [x] Document Swagger exploration examples and current-time versus reference-time behavior.
- [x] Document dashboard formulas, global counters, and unsupported order creation and coupon redemption flows.

## Validation

- [x] Run `./scripts/spec-status.sh` and `./scripts/check-specs.sh`.
- [x] Run `./scripts/validate.sh`.
- [x] Run PostgreSQL integration tests with Docker and `./mvnw verify`.
- [x] Run the documented load, inspect, repeated-load, reset, and rebuild walkthrough.
