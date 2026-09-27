# Spec: Documentation Governance

---
id: 014
status: completed
depends_on: [010, 011, 013]
last_updated: 2026-09-26
---

## Objective

Reduce documentation drift by validating objective consistency automatically and requiring an explicit documentation-impact decision for changes that need human judgment.

## Scope

- Add a dependency-free documentation validation command suitable for local development and CI.
- Validate local Markdown links and repository file references.
- Validate that the specification index contains every specification and reports a status consistent with its task checklist.
- Validate that documented specification ranges and environment variables remain aligned with repository sources of truth.
- Define a change-to-documentation responsibility matrix for API, configuration, database, observability, CI, and domain changes.
- Add a pull request template that requires authors to declare whether documentation was updated or why it is unaffected.
- Run fast documentation checks before commits and full validation before pushes.
- Run documentation validation in the required CI job.

## Out Of Scope

- Proving semantic correctness of prose without human review.
- Requiring a Markdown change for every source-code change.
- External documentation services, hosted link crawlers, or third-party GitHub Actions.
- Automatically rewriting documentation or specifications.
- Retrofitting historical commits or introducing a release-notes process.

## Business Rules

- Repository documentation is versioned with the behavior or infrastructure it describes.
- Objective inconsistencies must fail local and CI validation with actionable messages.
- Documentation-impact review must distinguish an updated document from a justified no-impact change.
- Checks must avoid requiring documentation changes for internal refactors that do not alter documented behavior.
- Local hooks improve feedback speed, while CI remains the authoritative shared enforcement point.
- Generated, remote, anchor-only, and example localhost links must not be treated as missing local files.

## Data Model

No application database or persistence changes are required.

## API Contracts

No application endpoints are added or changed. API contract changes remain governed by the OpenAPI and API documentation rules defined in spec 010.

## Validation

- Fail when a relative Markdown link targets a missing repository file or directory.
- Fail when a numbered specification directory is absent from the specification index or appears with an inconsistent completion status.
- Fail when an environment variable in `.env.example` is absent from the root README configuration reference.
- Fail when the root README reports an implemented specification range that does not include the latest completed specification.
- Print the document path and reason for every detected inconsistency.
- Keep the checker deterministic, offline, and free of additional runtime dependencies.

## Authorization

Not applicable. Repository contributors run local checks, and GitHub Actions runs the same checks with read-only repository permissions.

## Expected Errors

- A missing local target identifies the source document and unresolved link.
- A specification-index mismatch identifies the specification and expected status.
- An undocumented environment variable identifies the missing variable.
- A stale specification range identifies the expected completed range.
- Multiple problems are reported in one run before the command exits unsuccessfully.

## Migration Plan

No Flyway migration is required. Introduce scripts, hook integration, CI integration, the pull request template, and governance documentation incrementally.

## Cross-Spec Impact

- Extend spec 011's validation pipeline with a documentation stage.
- Preserve spec 010's OpenAPI contract and link API-impact guidance to its generated documentation and tests.
- Keep `specs/README.md` as the authoritative specification index.
- Update the root README and CI guide to describe the new commands and enforcement boundaries.

## Implementation Notes For Agents

- Read `AGENTS.md`, spec 013, this spec, and this spec's task list before implementation.
- Prefer portable Bash and standard Unix tools already required by the project.
- Keep fast checks in `pre-commit`; do not run the Maven suite there.
- Keep `pre-push` delegated to `./scripts/validate.sh`.
- Validate facts that have an explicit repository source of truth; route semantic judgment through the pull request template.
- Update tasks as each wave is completed and commit each completed wave separately.

## Non-Negotiable Constraints

- Keep code, documentation, messages, specs, tasks, and commits in English.
- Do not introduce Node.js, Python, or an external service solely for documentation validation.
- Do not make remote network availability a prerequisite for commits, pushes, or CI.
- Do not claim that automated checks prove semantic documentation correctness.
- Preserve existing validation, test, packaging, and static-analysis stages.

## Acceptance Criteria

- One documented command reports broken local documentation links, spec-index drift, stale completed-spec ranges, and undocumented environment variables.
- The command succeeds on a consistent repository and fails with actionable output on representative invalid fixtures or mutations.
- Pre-commit, pre-push validation, the CI-equivalent script, and GitHub Actions invoke the documentation checker at the appropriate stage.
- Pull requests require a visible documentation-impact declaration and justification when no update is needed.
- Contributors can determine which documents to review from a maintained change-impact matrix.
- The currently stale README specification range is corrected and protected against recurrence.

## Expected Tests

- Automated shell tests cover successful validation and each failure category using isolated temporary repositories or fixtures.
- Existing specification checks and application tests continue to pass.
- The CI-equivalent command includes and passes the documentation validation stage.

## Done Means

- All tasks are complete and the specification index reports this spec as completed.
- Documentation checks, standard validation, and the CI-equivalent command pass.
- Governance limitations and the human-review boundary are documented.
- Each implementation wave is committed with an English Conventional Commit message.

## Dependencies

- `010-api-documentation` for API documentation standards.
- `011-ci-pipeline` for shared CI enforcement.
- `013-api-journey-tests` as the immediately previous completed specification.
