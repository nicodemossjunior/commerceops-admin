# Tasks: Observability

## Backend

- [ ] Configure structured logging.
- [ ] Add request correlation filter.
- [ ] Add trace ID to global error responses.
- [ ] Add safe log redaction utility.
- [ ] Configure Spring Boot Actuator.
- [ ] Configure Micrometer metrics.
- [ ] Add domain metric counters where useful.
- [ ] Prepare OpenTelemetry configuration placeholders.

## Configuration

- [ ] Configure actuator exposure per profile.
- [ ] Configure service name.
- [ ] Configure environment name.
- [ ] Configure log level defaults.
- [ ] Configure local metrics endpoint.

## Tests

- [ ] Test correlation ID generation.
- [ ] Test trace ID appears in error response.
- [ ] Test sensitive values are not logged by helper utilities.
- [ ] Test actuator health endpoint availability in local profile.

## Documentation

- [ ] Document local actuator endpoints.
- [ ] Document structured log fields.
- [ ] Document sensitive data logging rules.
- [ ] Document future OpenTelemetry setup path.

## Validation

- [ ] Run unit tests.
- [ ] Run application context tests.
- [ ] Manually verify local actuator health endpoint after backend exists.

