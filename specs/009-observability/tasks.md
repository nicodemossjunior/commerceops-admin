# Tasks: Observability

## Backend

- [x] Configure structured logging.
- [x] Add request correlation filter.
- [x] Add trace ID to global error responses.
- [x] Add safe log redaction utility.
- [x] Configure Spring Boot Actuator.
- [x] Configure Micrometer metrics.
- [x] Add domain metric counters where useful.
- [x] Prepare OpenTelemetry configuration placeholders.

## Configuration

- [x] Configure actuator exposure per profile.
- [x] Configure service name.
- [x] Configure environment name.
- [x] Configure log level defaults.
- [x] Configure local metrics endpoint.

## Tests

- [x] Test correlation ID generation.
- [x] Test trace ID appears in error response.
- [x] Test sensitive values are not logged by helper utilities.
- [x] Test actuator health endpoint availability in local profile.

## Documentation

- [x] Document local actuator endpoints.
- [x] Document structured log fields.
- [x] Document sensitive data logging rules.
- [x] Document future OpenTelemetry setup path.

## Validation

- [ ] Run unit tests.
- [ ] Run application context tests.
- [ ] Manually verify local actuator health endpoint after backend exists.
