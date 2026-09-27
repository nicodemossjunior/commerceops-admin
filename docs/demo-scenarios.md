# Persistent demo scenarios

The demo is an explicit local tool. Normal application startup never generates data. It uses Java 21, Maven, Docker, PostgreSQL 16, and a dedicated `commerceops_demo` database on loopback port 5433. Your regular local database is unchanged.

## Load and explore

Supply your own local secrets in the shell (do not commit them):

```bash
export DEMO_DB_PASSWORD='replace-with-a-local-database-password'
export DEMO_ADMIN_PASSWORD='replace-with-a-local-admin-password'
export JWT_SECRET='replace-with-a-local-jwt-secret'
docker compose -f docker-compose.demo.yml up -d --wait
./scripts/demo.sh load --seed 42 --reference-time 2026-09-26T12:00:00Z
./scripts/demo.sh inspect
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Use a current UTC reference timestamp to explore current-period shortcuts. For repeatability, retain the same explicit timestamp on subsequent loads. Omitting it selects the current instant and therefore does not reproduce a previous load. `DEMO_DB_PORT` and `DEMO_DB_USERNAME` can override the demo port and user; database name and host are fixed. The admin password must have at least 12 characters. `DASHBOARD_LOW_STOCK_THRESHOLD` defaults to 10 and should be consistent for loading and serving the API.

Open `http://localhost:8080/swagger-ui/index.html`. Log in through `POST /api/auth/login` with `demo.admin@example.com` and your `DEMO_ADMIN_PASSWORD`. Copy the returned `accessToken` into Swagger's Authorize dialog.

The dataset contains 10 categories, 100 products, 200 customers, 30 notes, 500 orders/items, 20 coupons, order histories, audit records, and one admin. Two business records are soft-deleted: product 99 and customer 199. Categories include inactive records; products cover all statuses and stock below, at, and above the threshold. The final 50 customers have no orders. Coupons include fixed/percentage discounts, expired/future dates, activation states, and illustrative usage counters. Usage counters are fixtures; coupon redemption is not implemented.

Try:

```text
GET /api/products?sku=DEMO-SKU-0
GET /api/orders?status=PAID&sort=orderNumber,asc&page=0&size=10
GET /api/customers?email=demo.customer.0@example.com
GET /api/coupons?code=DEMO-COUPON-1
GET /api/dashboard/summary?period=CUSTOM&from=2026-08-27T12:00:00Z&to=2026-09-26T12:00:00Z
```

Take public UUIDs from responses to inspect details, edit registrations, change order statuses, cancel, or refund through the existing APIs. Initial orders are created internally because there is no public order creation endpoint. Order item names preserve their original snapshots, including after product 0 was renamed and product 99 was deleted.

## Manifest and expected results

`inspect` prints the stored JSON manifest, including version, seed, reference time, owned public UUIDs, counts, custom period, independent expected order metrics, and API examples. Metadata is stored in `demo_support.manifest` and `demo_support.owned` in the same PostgreSQL transaction as the load. No generated manifest or credential file is required in the repository.

The expected values describe the initial scenario only. After interactive edits, the manifest retains the original expectations. Repeated loading verifies ownership and existence, preserves those edits, and does not recreate deleted/missing records. Input conflicts or stale ownership fail explicitly.

Revenue follows the current API: gross excludes cancelled orders; net also excludes refunded orders. Pending orders remain included. Average order value is net revenue divided by the count excluding cancelled/refunded orders, rounded to two places. Customer count (199) and low-stock count (60 at the default threshold) are global and do not follow the selected period. Recent-order and low-stock lists contain at most five records.

The 90-day distribution includes custom boundaries. A custom interval one to two days after the reference time has zero order metrics but retains global customer/stock counts. Current shortcuts and coupon expiration use the running application's clock, not the dataset reference time, so results change as the dataset ages.

## Repeat, reset, and rebuild

Before making interactive changes, the following sequence is repeatable:

```bash
./scripts/demo.sh load --seed 42 --reference-time 2026-09-26T12:00:00Z
./scripts/demo.sh inspect
./scripts/demo.sh reset
./scripts/demo.sh load --seed 42 --reference-time 2026-09-26T12:00:00Z
```

Reset deletes only explicitly owned UUIDs and owned admin role assignments, in dependency order, in one transaction. Unrelated rows are preserved. Missing ownership metadata, missing owned records, unrelated foreign-key dependents, and unrelated audit/deletion references cause refusal and rollback. Interactive logins and mutations create new audit entries outside the original manifest; these deliberately block reset when they reference demo records. Inspect those dependencies and remove only disposable records through an explicit operator decision, or start a fresh dedicated demo database. The tool never guesses ownership or truncates business tables.

Flyway applies normal schema migrations only after target validation. Demo metadata tables are created exclusively by the explicit command and are not normal Flyway migrations. Failed fixture loads and resets roll back their data and metadata changes. Command errors avoid dumping database rows or credentials.

## Verification

```bash
./scripts/validate.sh
./mvnw verify
```

Unit tests cover deterministic generation and independent arithmetic. PostgreSQL Testcontainers tests cover load/inspect/repeat/reset/rebuild, unrelated data, conflicts, rollback, unsafe targets, empty ordinary startup, JWT login, filters, and dashboard results. Integration tests require Docker and never use the persistent demo database.
