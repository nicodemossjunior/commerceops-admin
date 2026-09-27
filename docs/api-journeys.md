# Automated API journeys

## Run

Requirements: Java 21, Maven wrapper, and a running Docker-compatible daemon. Testcontainers starts disposable PostgreSQL 16 containers and applies the ordinary Flyway migrations, including PostgreSQL partial unique indexes. No local database or application secrets are required. Missing Docker fails the integration stage; tests are not silently skipped.

```bash
# Fast existing tests and specification checks; does not execute *IT.java.
./scripts/validate.sh

# Unit tests, packaging, and all PostgreSQL integration tests.
./mvnw verify

# Compile, unit/integration tests, packaging, and Checkstyle, matching CI.
./scripts/ci.sh

# Only the HTTP journey suite.
./mvnw -Dskip.unit.tests=true -Dit.test=ApiJourneysIT verify

# One independent journey (quote the # for shell portability).
./mvnw -Dskip.unit.tests=true '-Dit.test=ApiJourneysIT#catalogRegistrationFiltersPaginationAndSoftDeletion' verify
```

JUnit/FailSafe reports are in `target/failsafe-reports`; existing fast-test reports are in `target/surefire-reports`. CI uploads both report directories even when a test fails. HTTP failure assertions identify the method, path, expected status, and actual status without printing request bodies, credentials, or JWTs.

## Isolation and fixtures

`ApiJourneysIT` starts the real application on a random HTTP port. Each test clears only its Testcontainers database after checking the database name, provisions five role accounts using the real password encoder, and logs in through HTTP. All supported business mutations use HTTP, and the suite does not mock security, persistence, services, or current users.

Direct preparation is confined to prerequisites without public APIs: role accounts, initial pending orders/items and historical dates, coupon usage counters, and one arbitrary internal audit event for redaction verification. Order prices reuse the pure `DemoScenario.price` convention from spec 012. These tests never run the persistent demo loader or read its manifest. HTTP writes are committed in server transactions, so cleanup is explicit instead of relying on a test-method rollback.

The dashboard resolver uses a fixed test-only clock of `2026-09-26T12:00:00Z`. Its production implementation is unchanged. Boundary fixtures use PostgreSQL microsecond precision and include adjacent excluded records. JWT expiration is tested with an authentically signed expired token derived from a login response; no sleeps are needed. Coupon effective-expiration fixtures use a fixed past date, while eligibility windows are queried with explicit timestamps.

## Operation and filter coverage matrix

All method references below are in `src/test/java/com/commerceops/admin/journeys/ApiJourneysIT.java`.

| API family | Operations and assertions | Test methods |
| --- | --- | --- |
| Authentication | POST login, GET identity; real JWT, bad credentials, missing/invalid/expired tokens, validation error shape | `authenticationRejectsMissingInvalidExpiredTokensAndBadCredentials` |
| Categories | POST, GET list/detail, PUT including status, DELETE; duplicate slug, active-product deletion guard, persisted edits, soft delete, slug reuse | `catalogRegistrationFiltersPaginationAndSoftDeletion` |
| Products | POST, GET list/detail, PUT including status/price/stock, DELETE; duplicate SKU, foreign category lookup, price validation, snapshots, SKU reuse | `catalogRegistrationFiltersPaginationAndSoftDeletion`, `invalidRequestsHaveStableErrorsAndDoNotCreateBusinessRecords`, `orderLifecycleFiltersHistoryAndHistoricalSnapshots` |
| Product filters | `categoryId`, `status`, partial case-insensitive `sku`/`name`, inclusive `minPrice`/`maxPrice`, both `lowStock` values, combined criteria | `catalogRegistrationFiltersPaginationAndSoftDeletion` |
| Customers | POST, GET list/detail, PUT including status, DELETE; duplicate email, reuse after soft delete, persisted changes | `customersNotesAndSoftDeletedEmailReuse` |
| Customer filters | `name`, `email`, `phone`, `status`, each separately and combined | `customersNotesAndSoftDeletedEmailReuse` |
| Customer notes | GET list, POST, DELETE; actual note content, invalid blank note, mismatched customer, persistence after deletion | `customersNotesAndSoftDeletedEmailReuse` |
| Purchase history | GET customer orders; empty history, matching order UUID, pagination and permissions | `customersNotesAndSoftDeletedEmailReuse`, `orderLifecycleFiltersHistoryAndHistoricalSnapshots`, `rolePermissionsUseRealTokensAndRejectedWritesPreserveState` |
| Coupons | POST, GET list/detail, PUT, PATCH activate/deactivate, DELETE; normalization, duplicates, expiration, forbidden activation, code reuse | `couponLifecycleValidityFiltersAndCodeReuse` |
| Coupon filters | `code`, effective `status`, `discountType`, `activeAt`, separately and combined; inclusive start, exclusive end, exhausted usage | `couponLifecycleValidityFiltersAndCodeReuse`, `couponEligibilityIncludesStartExcludesEndAndRespectsUsageLimit` |
| Orders | GET list/detail, PATCH status, POST cancel/refund; full fulfillment path, payment/delivery summaries, totals, exact history, reasons, forbidden transitions, product snapshots | `orderLifecycleFiltersHistoryAndHistoricalSnapshots` |
| Order filters | `orderNumber`, `customerId`, `status`, `paymentStatus`, `deliveryStatus`, `createdFrom`, `createdTo`, separately and combined; inclusive and adjacent time boundaries | `orderLifecycleFiltersHistoryAndHistoricalSnapshots`, `dashboardPeriodsIncludeBoundariesAndExcludeAdjacentRecords` |
| Audit | GET list/detail; actors, action, target UUID, HTTP context, persisted event for each audited catalog/customer/coupon/order mutation; login secrets absent | `auditRecordsActorsTargetsFiltersAndSafeMetadata`, domain journey calls to `audited` |
| Audit filters | `actorUserId`, `actorEmail`, `action`, `entityType`, `entityPublicId`, `createdFrom`, `createdTo`, separately and combined; invalid range | `auditRecordsActorsTargetsFiltersAndSafeMetadata` |
| Audit redaction | Nested sensitive values redacted in stored JSON and returned detail; safe context retained | `auditDetailRedactsSensitiveMetadataFromInternalEvents` |
| Dashboard | GET summary; exact gross/net revenue, average, counts, cancellation/refund effects, current global counters, soft deletion, empty orders | `dashboardTracksOperationsAndRetainsGlobalCountersForEmptyPeriods` |
| Dashboard periods | Default, TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, CUSTOM; exact resolved periods, inclusive boundaries, adjacent exclusions | `dashboardPeriodsIncludeBoundariesAndExcludeAdjacentRecords` |
| Dashboard lists/errors | Recent and low-stock ordering, five-item limits, invalid enum, missing/reversed custom dates | `dashboardValidatesRangesAndLimitsSupportingLists` |
| Pagination and sorting | `page`, `size`, `sort`; page metadata and exact UUID order across pages without duplicates for categories/products/customers/notes/coupons/orders/audit | Domain methods using `assertPages` |
| Errors and identifiers | Required fields and field errors, invalid enums/UUIDs, missing resources, duplicate conflicts, invalid relationships and discounts; stable status/code/path/time/trace; public UUIDs in entity responses | `invalidRequestsHaveStableErrorsAndDoNotCreateBusinessRecords`, domain journeys and common HTTP helpers |
| Authorization | All five roles, all scoped business writes, allowed reads and restricted notes/audit/dashboard, denied mutations leave existing state intact | `rolePermissionsUseRealTokensAndRejectedWritesPreserveState` (five invocations) |

Categories have no domain filters beyond pagination/sorting. Notes support creation and deletion, not editing. Orders have no creation endpoint. The suite does not assume checkout, coupon redemption, or stock changes on order transitions.

## Permission matrix exercised

| Operation | ADMIN | MANAGER | SUPPORT | CATALOG | READ_ONLY |
| --- | --- | --- | --- | --- | --- |
| Read catalog/customers/orders/coupons/history | Yes | Yes | Yes | Yes | Yes |
| Create/delete categories/products | Yes | No | No | Yes | No |
| Update categories/products | Yes | Yes | No | Yes | No |
| Write customers/coupons and operate orders | Yes | Yes | No | No | No |
| Read/write notes | Yes | No | Yes | No | No |
| Read audit | Yes | Yes | No | No | No |
| Read dashboard | Yes | Yes | Yes | No | Yes |

## Dashboard expectations

Assertions use small hand-calculated fixtures: three pending orders of 10.00 produce gross/net 30.00. Cancelling one and refunding another produces gross 20.00, net 10.00, and average 10.00. This follows the existing API, including pending orders in revenue. Customer and stock counters are global, while order metrics and recent orders use the selected creation period. This suite does not redefine those values as settled payments or historical inventory accounting.
