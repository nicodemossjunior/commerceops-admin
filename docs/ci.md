# Continuous Integration

CommerceOps Admin uses GitHub Actions to validate every pull request and every push to `main`. The workflow is defined in `.github/workflows/ci.yml` and requires no application secrets for normal validation.

## Pipeline Stages

The `validate` job runs these stages in order:

1. Check out the repository.
2. Install Temurin Java 21 and restore the Maven dependency cache.
3. Compile application and test sources.
4. Run unit tests with Maven Surefire.
5. Confirm that Docker is available for Testcontainers.
6. Run integration tests with Maven Failsafe.
7. Build the executable JAR without rerunning tests.
8. Run Checkstyle static analysis.
9. Upload the JAR as a workflow artifact for seven days.

The Maven cache key is derived from `pom.xml`. A compilation error, test failure, integration-test failure, missing artifact, or Checkstyle violation fails the job.

## Running the Same Checks Locally

Run the complete local equivalent from the repository root:

```bash
./scripts/ci.sh
```

The individual commands are:

```bash
./scripts/check-specs.sh
./mvnw --batch-mode --no-transfer-progress clean test-compile -DskipTests
./mvnw --batch-mode --no-transfer-progress -Dskip.integration.tests=true test
./mvnw --batch-mode --no-transfer-progress -Dskip.unit.tests=true verify
./mvnw --batch-mode --no-transfer-progress -DskipTests package
./mvnw --batch-mode --no-transfer-progress checkstyle:check
```

The generated application artifact is `target/commerceops-admin-0.0.1-SNAPSHOT.jar`.

## Unit and Integration Tests

Unit and application-context tests use the `*Test.java` naming convention and run through Maven Surefire. Integration tests use `*IT.java` and run through Maven Failsafe during `verify`.

There are currently no `*IT.java` tests. The integration phase still runs successfully and will automatically discover tests when they are added.

Database integration tests should use Testcontainers with PostgreSQL instead of a shared database. They require:

- A running Docker-compatible daemon.
- Permission to create and remove containers.
- Network access for the first image pull.
- No fixed database username, password, port, or other local secret.

GitHub-hosted Ubuntu runners provide Docker. The CI workflow executes `docker info` before the integration phase so missing Docker support fails with a clear message. The local script only requires Docker after an `*IT.java` test exists.

## Environment and Secrets

Normal CI validation uses the test profile and its in-memory H2 database. It does not require `.env`, PostgreSQL credentials, JWT production secrets, cloud credentials, or registry credentials. The repository's test-only JWT value is not a production secret.

The workflow has read-only repository-content permission and does not print environment variables. Secrets must not be added to Maven command lines or diagnostic output.

## Future Docker Image Stage

The `docker-build` job is reserved in the workflow and is disabled by default. It runs only when the repository variable `ENABLE_DOCKER_BUILD` is explicitly set to `true`. No image is currently published.

Before enabling that job, the repository must provide a reviewed `Dockerfile`. Publishing to a container registry additionally requires decisions about:

- Registry provider and image naming.
- Authentication through short-lived credentials or a repository secret.
- Immutable version and commit-SHA tags.
- Image provenance and vulnerability scanning.
- Retention and deployment environments.

Registry login and push steps must remain absent until the deployment strategy and secret ownership are defined.
