# Tasks: Documentation Governance

## Specification and Policy

- [x] Define the objective, automation boundaries, and acceptance criteria.
- [x] Add spec 014 to the specification index.
- [ ] Document the change-to-documentation responsibility matrix.
- [ ] Define the required pull request documentation-impact declaration.

## Documentation Checker

- [ ] Add an offline, dependency-free documentation validation script.
- [ ] Validate relative Markdown file and directory links.
- [ ] Validate specification index coverage and task-derived status.
- [ ] Validate `.env.example` variables against the root README.
- [ ] Validate the completed-spec range reported by the root README.
- [ ] Report all detected inconsistencies with actionable messages.

## Automated Tests

- [ ] Add isolated success and failure tests for the documentation checker.
- [ ] Cover broken links, missing spec entries, incorrect spec status, undocumented variables, and stale spec ranges.
- [ ] Ensure tests do not mutate the working tree or require network access.

## Hooks and CI

- [ ] Run documentation checks from the pre-commit hook.
- [ ] Run documentation checks from `./scripts/validate.sh` and `./scripts/ci.sh`.
- [ ] Add an explicit documentation validation step to GitHub Actions.
- [ ] Add a pull request template with documentation-impact choices and justification.

## Documentation

- [ ] Add a documentation governance guide and responsibility matrix.
- [ ] Correct the stale specification range in the root README.
- [ ] Document the validation command, hook behavior, CI enforcement, and semantic-review limitation.
- [ ] Update the CI guide and root README with the new validation stage.

## Validation

- [ ] Run the documentation checker tests.
- [ ] Run `./scripts/spec-status.sh` and `./scripts/check-specs.sh`.
- [ ] Run `./scripts/validate.sh`.
- [ ] Run `./scripts/ci.sh`.
- [ ] Confirm each implementation wave has a focused Conventional Commit.
