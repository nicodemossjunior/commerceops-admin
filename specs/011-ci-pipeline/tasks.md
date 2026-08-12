# Tasks: CI Pipeline

## CI Configuration

- [ ] Create CI workflow file.
- [ ] Configure trigger for pull requests.
- [ ] Configure trigger for pushes to main.
- [ ] Set up Java 21.
- [ ] Configure Maven dependency cache.
- [ ] Run Maven compile.
- [ ] Run unit tests.
- [ ] Run integration tests when available.
- [ ] Run package build.
- [ ] Add static analysis step if selected.

## Testcontainers

- [ ] Ensure CI environment supports Docker for Testcontainers.
- [ ] Document Testcontainers requirements.
- [ ] Separate unit and integration test phases if needed.

## Future Docker Build

- [ ] Reserve stage for Docker image build.
- [ ] Document future container registry requirements.
- [ ] Keep Docker publishing disabled until deployment strategy is defined.

## Documentation

- [ ] Document CI stages.
- [ ] Document how to run the same checks locally.
- [ ] Document required environment assumptions.

## Validation

- [ ] Run the CI-equivalent Maven commands locally.
- [ ] Confirm CI does not require secrets for normal validation.
- [ ] Confirm workflow is readable for portfolio review.

