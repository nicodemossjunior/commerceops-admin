# Documentation Governance

CommerceOps Admin treats documentation as a versioned part of each change. Objective consistency is checked automatically, while contributors explicitly review semantic impact in every pull request.

## Guarantees and Boundaries

`./scripts/check-docs.sh` validates facts with repository-owned sources of truth:

- Relative Markdown links resolve to existing files or directories.
- Every numbered specification appears in `specs/README.md` at the correct path.
- Specification index statuses match their task completion state.
- Every variable in `.env.example` is referenced by the root README.
- The completed specification range in the root README reaches the latest completed spec.

The checker is deterministic, offline, and has no runtime dependency beyond Bash and standard Unix tools. It reports all problems found in one run.

Automation cannot determine whether prose accurately explains a business rule or whether an internal code change affects users. Pull request authors and reviewers remain responsible for semantic accuracy. A source-code change does not automatically require a Markdown edit when documented behavior is unchanged.

## Change Responsibility Matrix

| Change area | Review these sources | Update when |
| --- | --- | --- |
| Controllers, request/response DTOs, validation, or authorization | Active spec, OpenAPI annotations/tests, `docs/api.md` | An endpoint, field, status, error, filter, example, or role requirement changes. |
| Domain services and business rules | Active spec, API guide, relevant operational guide | User-visible behavior, calculations, lifecycle rules, or failure conditions change. |
| Environment variables and Spring configuration | `.env.example`, root README, relevant operational guide | A setting is added, renamed, removed, or its default or purpose changes. |
| Flyway migrations and persistence rules | Active spec, root README database section, API guide when visible | Schema, constraints, deletion behavior, identifiers, or operational setup changes. |
| Logging, metrics, Actuator, or tracing | `docs/observability.md`, root README, active spec | Signals, exposure, security, configuration, or troubleshooting behavior changes. |
| Maven, scripts, hooks, or GitHub Actions | `docs/ci.md`, root README commands, active spec | Prerequisites, validation stages, artifacts, triggers, or contributor workflow changes. |
| Demo scenarios and API journeys | `docs/demo-scenarios.md`, `docs/api-journeys.md`, coverage matrix, active spec | Fixtures, commands, supported journeys, or expected results change. |
| Authentication or bootstrap behavior | Root README, `docs/api.md`, active spec | Credentials, tokens, roles, provisioning, or access rules change. |

## Local Workflow

Run the documentation checks directly:

```bash
./scripts/test-check-docs.sh
./scripts/check-docs.sh
```

With versioned hooks enabled, `pre-commit` runs the fast spec and documentation consistency checks. `pre-push` runs `./scripts/validate.sh`, which also executes the checker tests and application test suite.

```bash
git config core.hooksPath .githooks
```

Hooks provide early feedback but can be absent or bypassed. GitHub Actions repeats the checks and is the shared enforcement point. Protecting `main` with the CI job as a required status check prevents merging a known inconsistency.

## Pull Request Review

The pull request template requires exactly one documentation-impact declaration:

1. Documentation was updated, with the relevant files identified.
2. No documentation update is needed, with a concrete reason.

A no-impact declaration is appropriate for internal refactoring, test-only strengthening, or implementation fixes that restore already documented behavior. It is not appropriate when a public contract, business rule, configuration surface, operational procedure, or contributor workflow changes.

Reviewers should compare the change with the responsibility matrix and reject unexplained or contradictory declarations. The declaration provides the semantic decision that an automated link or consistency checker cannot make.
