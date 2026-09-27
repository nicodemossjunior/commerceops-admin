# Spec: Persistent Demo Scenarios

---
id: 012
status: completed
depends_on: [011, 002, 003, 004, 005, 006, 007, 008]
last_updated: 2026-09-26
---

## Objective

Provide a repeatable local PostgreSQL dataset for exploring registrations, filters, order operations, audit records, and dashboard metrics through the existing API and Swagger UI.

## Scope

- Add an explicitly invoked local demo command with load, inspect, and reset operations.
- Generate persistent categories, products, customers, customer notes, coupons, orders, items, and coherent order histories.
- Provide a default scenario with 10 categories, 100 products, 200 customers, 500 orders, and 20 coupons spread over 90 days.
- Support a fixed random seed and an explicit UTC reference timestamp.
- Produce a scenario manifest with input parameters, logical record identifiers, counts, useful filter examples, and expected dashboard results.
- Document commands to load data, explore it, and rebuild the demo dataset.

## Out Of Scope

- Frontend projects, screens, charts, and browser automation.
- Public order creation endpoints, checkout, payment processing, and coupon redemption.
- Load testing, production data imports, or changes to existing business rules.
- The full HTTP journey suite, which belongs to spec 013.

## Business Rules

- Loading data requires both an explicit demo profile and an explicit command; ordinary application startup must not load or reset demo data.
- Use a dedicated local demo database. Validate its configured identity before writes and reject an ordinary application database target.
- Loading the same scenario version, seed, and reference timestamp twice must not duplicate records. Reject conflicting inputs for an existing dataset and explain the reset procedure.
- Reset may remove only records owned by the scenario, including dependent items, histories, notes, and audit entries. Never truncate shared business tables or delete unrelated records.
- Refuse reset if unrelated records reference scenario-owned data; report the conflict without partial deletion.
- Load and reset must be atomic, or provide equivalent tested recovery so a failure cannot be mistaken for a complete dataset.
- Use synthetic names and reserved example email domains. Do not embed real customer data or committed credentials.
- Include active and inactive records, supported product statuses, empty and low stock, deleted records, and customers with and without purchase history.
- Include all supported order statuses, varied item quantities, discounts, shipping, and consistent totals and payment/delivery summaries.
- Histories must follow valid transitions and timestamp ordering. Cancelled and refunded orders must include their required reasons and metadata.
- Include fixed and percentage coupons, future and expired validity windows, activation states, and usage-limit examples supported by the current model. Do not imply coupon redemption is implemented.
- Spread order creation dates across today, the last 7 and 30 days, the current month, and older dates relative to the reference timestamp. Include custom-period boundaries and an empty period.
- Define stock values around the configured low-stock threshold.
- Preserve historical item snapshots when a referenced product is edited or soft-deleted.
- Reference time controls generated dates; it does not change the running API clock. Document how current-time shortcuts and coupon expiration change as the dataset ages.

## Data Model

Reuse existing domain tables and constraints. Track scenario ownership and version explicitly, using a local manifest or demo-only metadata, including public IDs needed for safe reset. A naming prefix alone is insufficient proof of ownership.

The manifest must record the seed, reference timestamp, scenario version, record counts, and expected values for documented custom periods. Reproducibility applies to generated business values, relationships, and expected results; database sequence IDs and security credentials need not be identical.

## API Contracts

No new public REST endpoints are required. Existing catalog, customer, coupon, order, audit, and dashboard endpoints remain unchanged.

Proposed command contract:

```text
./scripts/demo.sh load --seed 42 --reference-time 2026-09-26T12:00:00Z
./scripts/demo.sh inspect
./scripts/demo.sh reset
```

The script must select the dedicated demo configuration explicitly. Credentials come from local configuration or environment variables and must not appear in the manifest or logs.

Orders have no public creation endpoint. Prepare initial orders through a local fixture loader, then use existing domain services for lifecycle operations where practical. Any historical timestamp setup stays confined to demo infrastructure.

## Validation

- Validate seed, UTC timestamp, supported scenario version, target database identity, and required configuration before mutation.
- Enforce existing uniqueness, foreign keys, monetary constraints, soft-delete rules, and valid order transitions.
- Check the manifest against persisted records before treating an existing load as complete.
- Reject missing, stale, or conflicting ownership information rather than guessing what can be deleted.

## Authorization

- The loader is a local operator command, not a remotely accessible API capability.
- API exploration uses existing JWT authentication and role permissions.
- Demo actors needed for histories and auditing must have explicit ownership; use existing password hashing and externally supplied credentials if interactive login is supported.

## Expected Errors

Command errors must be in English and return a nonzero exit status for invalid arguments, unsafe database targets, ownership conflicts, or failed persistence. Existing API error contracts remain unchanged. Error output must not expose credentials.

## Migration Plan

- Do not insert demo business data through normal Flyway migrations.
- Run existing Flyway migrations on the dedicated demo database.
- Prefer a versioned local manifest for ownership. If database metadata is necessary, isolate it to demo configuration and document its schema and cleanup behavior.

## Cross-Spec Impact

- Reuse domain rules from specs 002 through 008 and local environment conventions from 001.
- Provide reusable deterministic fixture definitions for spec 013 without making its tests depend on a persistent local database or manifest.
- Preserve dashboard semantics: the period filters orders and related metrics; customer and low-stock counts describe current non-deleted records globally.
- Expected revenue must match current rules: gross excludes cancelled orders; net excludes cancelled and refunded orders. Other statuses remain included. Average order value divides net revenue by non-cancelled, non-refunded order count, with zero for an empty denominator.

## Implementation Notes For Agents

- Read `AGENTS.md`, this spec and its tasks, and verify spec 011 is complete before implementation.
- Use Java 21 and existing domain components; keep fixture infrastructure separate from ordinary application startup.
- Calculate documented expectations independently of the production dashboard query.
- Update `tasks.md` as implementation progresses and run `./scripts/validate.sh` before finishing.

## Non-Negotiable Constraints

- Keep code, documentation, messages, specs, tasks, and commits in English.
- Do not introduce frontend code or expand public API contracts.
- Never modify an ordinary local or production database as a side effect of startup.
- Do not commit credentials, generated local manifests, or real personal data.

## Acceptance Criteria

- One documented load command creates the default dataset and leaves it available after the command exits.
- Repeating the load with identical inputs preserves counts and business values.
- Invalid inputs and unsafe targets fail before writes.
- The manifest identifies representative records and reproducible filter and custom-period dashboard examples.
- Reset removes only scenario-owned data, preserves unrelated data, and permits an identical rebuild.
- Interrupted or failed operations do not leave a dataset incorrectly marked complete.
- Existing API behavior and normal application startup remain unchanged.

## Expected Tests

- Deterministic generation, date distribution, totals, and fixture relationship tests.
- PostgreSQL integration tests for load, repeated load, reset, ownership conflicts, and rollback on failure.
- Tests that normal startup and invalid target configurations cannot load demo data.
- Verification of representative filters, historical snapshots, and independently calculated dashboard expectations.

## Done Means

- All implementation tasks are checked and documentation includes a complete exploration walkthrough.
- `./scripts/validate.sh` and PostgreSQL integration tests pass.
- The implementation is committed with an English Conventional Commit message.

## Dependencies

- Specs 001 through 008 for environment and business behavior.
- `011-ci-pipeline` for validation conventions.
