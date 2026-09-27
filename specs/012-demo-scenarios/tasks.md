# Tasks: Persistent Demo Scenarios

## Backend

- [ ] Define versioned scenario inputs, ownership tracking, and manifest format.
- [ ] Implement deterministic generators for all scoped entities and historical dates.
- [ ] Implement coherent order lifecycle histories and historical item snapshots.
- [ ] Implement explicit load, inspect, and reset commands.
- [ ] Require demo configuration and validate the dedicated database identity before writes.
- [ ] Implement idempotent loading, conflict detection, atomic failure handling, and ownership-scoped reset.
- [ ] Generate independent expected metrics and representative filter examples.

## Database and Flyway

- [ ] Configure a dedicated local demo PostgreSQL database using existing migrations.
- [ ] Keep demo data out of ordinary Flyway migrations and normal startup.
- [ ] Verify foreign keys, uniqueness, soft delete, and cleanup dependency order.
- [ ] Ignore generated manifests and local credentials in version control where needed.

## Tests

- [ ] Test deterministic inputs, monetary totals, relationships, and time boundaries.
- [ ] Test PostgreSQL load, repeat load, reset, and rebuild.
- [ ] Test unsafe targets, ownership conflicts, unrelated record preservation, and transaction failure recovery.
- [ ] Verify representative API filters, historical snapshots, and dashboard expectations.
- [ ] Verify ordinary application startup does not generate demo data.

## Documentation

- [ ] Document database setup, credentials, commands, dataset composition, and reset behavior.
- [ ] Document Swagger exploration examples and current-time versus reference-time behavior.
- [ ] Document dashboard formulas, global counters, and unsupported order creation and coupon redemption flows.

## Validation

- [ ] Run `./scripts/spec-status.sh` and `./scripts/check-specs.sh`.
- [ ] Run `./scripts/validate.sh`.
- [ ] Run PostgreSQL integration tests with Docker and `./mvnw verify`.
- [ ] Run the documented load, inspect, repeated-load, reset, and rebuild walkthrough.
