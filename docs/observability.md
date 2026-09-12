# Observability

CommerceOps Admin provides structured request logs, request correlation, Micrometer metrics, and profile-specific Actuator exposure.

## Request Correlation

Every HTTP response contains:

```text
X-Request-Id
X-Trace-Id
```

Clients may supply either header using up to 100 letters, numbers, dots, underscores, or hyphens. Missing or invalid values are replaced with generated UUIDs. The resolved trace ID is included in API error responses and audit records.

## Structured Logs

Console logs use Spring Boot's Logstash JSON format by default. Request completion events contain the following fields when applicable:

```text
@timestamp
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

`spanId` is reserved for a future tracing bridge and is present only when a tracing implementation supplies it. `userId` is the authenticated user's public UUID and is absent for anonymous requests.

Configure logging with:

```text
APP_ENVIRONMENT
ROOT_LOG_LEVEL
APPLICATION_LOG_LEVEL
CONSOLE_LOG_FORMAT
```

## Sensitive Data

Passwords, password hashes, JWTs, bearer tokens, access or refresh tokens, authorization values, API keys, credentials, and secrets must never be passed directly to a logger.

Request logging intentionally excludes bodies, query strings, and headers. When a value from an external source must be logged, pass it through `SensitiveLogSanitizer` first. Authentication metrics use counters without email or user ID tags.

Spring MVC request and response body loggers remain at `INFO` even when broader debugging is enabled. This prevents login passwords and issued JWTs from appearing in diagnostic logs.

## Local Actuator Endpoints

The `local` profile exposes:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
GET /actuator/prometheus
```

`health` and `info` are public. `metrics` and `prometheus` require authentication. Other profiles expose only `health` and `info` unless explicitly overridden.

Example after obtaining a JWT:

```bash
curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://localhost:8080/actuator/metrics
curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://localhost:8080/actuator/prometheus
```

Application meters include:

```text
http.server.requests
commerceops.orders.created
commerceops.orders.by_status
commerceops.products.low_stock
commerceops.auth.login.success
commerceops.auth.login.failure
commerceops.coupons.active
```

The only domain metric tag is the bounded `status` value on `commerceops.orders.by_status`. Raw emails, user IDs, entity IDs, and other high-cardinality values are not metric tags.

## Future OpenTelemetry Path

Tracing is disabled by default. The following placeholders reserve configuration without requiring a collector:

```text
OTEL_TRACING_ENABLED
OTEL_TRACES_SAMPLER_PROBABILITY
OTEL_EXPORTER_OTLP_ENDPOINT
OTEL_EXPORTER_OTLP_PROTOCOL
```

To complete an OpenTelemetry rollout later:

1. Add the Micrometer OpenTelemetry tracing bridge and the desired OTLP exporter.
2. Bind the reserved endpoint and protocol properties to the exporter configuration.
3. Enable tracing and choose an environment-appropriate sampling probability.
4. Deploy an OpenTelemetry Collector and verify `traceId` and `spanId` propagation across service boundaries.

No collector, external exporter, or distributed tracing backend is started by this specification.
