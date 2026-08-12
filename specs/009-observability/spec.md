# Spec: Observability

## Objective

Prepare CommerceOps Admin for production-style observability with structured logs, request correlation, metrics, actuator endpoints, and tracing readiness.

## Scope

- Configure structured application logging.
- Add request correlation through request ID and trace ID.
- Expose safe Spring Boot Actuator endpoints.
- Add application and domain metrics.
- Prepare the codebase for future OpenTelemetry integration.
- Keep sensitive data out of logs.

## Out Of Scope

- Full OpenTelemetry collector setup.
- External log aggregation.
- Alerting rules.
- Production dashboard tooling.
- Distributed microservice tracing.

## Structured Log Fields

Recommended fields:

```text
timestamp
level
service
environment
traceId
spanId
requestId
userId
method
path
status
durationMs
message
```

## Metrics

Initial metrics:

```text
http.server.requests
commerceops.orders.created
commerceops.orders.by_status
commerceops.products.low_stock
commerceops.auth.login.success
commerceops.auth.login.failure
commerceops.coupons.active
```

## Actuator

Local/dev endpoints:

```text
health
info
metrics
prometheus
```

Production-like exposure should be restricted.

## Business Rules

- Logs must not contain passwords, password hashes, JWTs, or secret values.
- Authentication failures can be counted but must not leak credential details.
- Request correlation must be available in error responses through `traceId`.
- Metrics should avoid high-cardinality labels such as raw user IDs or emails.

## Authorization

- Actuator endpoint exposure must be environment-specific.
- Sensitive actuator endpoints must not be publicly exposed.
- Application metrics are not exposed through business API endpoints.

## Expected Errors

- Observability components should not introduce business API errors.
- Logging failures should not break request processing.

## Acceptance Criteria

- Requests receive correlation IDs.
- Error responses include `traceId`.
- Logs are structured consistently.
- Basic metrics are available through actuator in local development.
- Sensitive data is redacted from logs.
- The project is ready for future OpenTelemetry setup.

## Expected Tests

- Correlation ID filter test.
- Error response includes trace ID test.
- Sensitive data redaction test.
- Actuator configuration test where practical.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.

