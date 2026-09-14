# Tasks: CI Pipeline

## CI Configuration

- [x] Create CI workflow file.
- [x] Configure trigger for pull requests.
- [x] Configure trigger for pushes to main.
- [x] Set up Java 21.
- [x] Configure Maven dependency cache.
- [x] Run Maven compile.
- [x] Run unit tests.
- [x] Run integration tests when available.
- [x] Run package build.
- [x] Add static analysis step if selected.

## Testcontainers

- [x] Ensure CI environment supports Docker for Testcontainers.
- [ ] Document Testcontainers requirements.
- [x] Separate unit and integration test phases if needed.

## Future Docker Build

- [x] Reserve stage for Docker image build.
- [ ] Document future container registry requirements.
- [x] Keep Docker publishing disabled until deployment strategy is defined.

## Documentation

- [ ] Document CI stages.
- [ ] Document how to run the same checks locally.
- [ ] Document required environment assumptions.

## Validation

- [ ] Run the CI-equivalent Maven commands locally.
- [ ] Confirm CI does not require secrets for normal validation.
- [ ] Confirm workflow is readable for portfolio review.
