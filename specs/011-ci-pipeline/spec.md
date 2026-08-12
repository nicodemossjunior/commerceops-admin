# Spec: CI Pipeline

## Objective

Create a CI pipeline that validates CommerceOps Admin through dependency setup, compilation, tests, integration tests, static checks, and future Docker image preparation.

## Scope

- Configure CI for Maven-based Java 21 backend.
- Run unit tests.
- Run integration tests when available.
- Cache Maven dependencies.
- Validate build.
- Prepare structure for future Docker image build.
- Keep CI configuration readable for portfolio review.

## Out Of Scope

- Production deployment.
- Cloud infrastructure provisioning.
- Release automation.
- Frontend CI.
- Container registry publishing.

## Pipeline Stages

Initial stages:

```text
checkout
setup-java
cache-maven
compile
unit-tests
integration-tests
package
static-analysis
```

Future stages:

```text
docker-build
security-scan
staging-deploy
production-deploy
```

## CI Requirements

- Use Java 21.
- Use Maven.
- Fail on compilation errors.
- Fail on test failures.
- Integration tests should use Testcontainers when database access is needed.
- CI must not require local secrets for normal build and test.
- Secrets must never be printed in logs.

## Acceptance Criteria

- CI runs on pull requests.
- CI runs on pushes to the main branch.
- Maven dependencies are cached.
- Unit tests run successfully.
- Integration tests run successfully when present.
- Build artifact is generated.
- CI file is easy to understand and maintain.

## Expected Tests

- CI itself validates tests by running Maven.
- No separate application tests are required for the pipeline spec.

## Dependencies

- `000-project-foundation`.
- Test specs from implemented domains.

