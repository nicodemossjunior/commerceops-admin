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
- [x] Document Testcontainers requirements.
- [x] Separate unit and integration test phases if needed.

## Future Docker Build

- [x] Reserve stage for Docker image build.
- [x] Document future container registry requirements.
- [x] Keep Docker publishing disabled until deployment strategy is defined.

## Documentation

- [x] Document CI stages.
- [x] Document how to run the same checks locally.
- [x] Document required environment assumptions.

## Validation

- [x] Run the CI-equivalent Maven commands locally.
- [x] Confirm CI does not require secrets for normal validation.
- [x] Confirm workflow is readable for portfolio review.
