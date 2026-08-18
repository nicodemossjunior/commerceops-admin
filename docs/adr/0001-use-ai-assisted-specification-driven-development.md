# ADR 0001: Use AI-Assisted Specification Driven Development

## Status

Accepted

## Context

CommerceOps Admin is implemented in small, ordered specifications. AI agents can help move quickly, but they need repository-local rules and repeatable validation commands to avoid drifting across specs or mixing unrelated changes.

## Decision

The project will keep implementation guidance in `AGENTS.md`, specifications in `specs/`, reusable validation commands in `scripts/`, and optional local Git hooks in `.githooks/`.

Agents must implement specs in order, update `tasks.md`, use English across project artifacts, and run `./scripts/validate.sh` before finishing implementation work.

## Consequences

- New contributors and agents have one local entry point for repository rules.
- Spec structure can be checked mechanically.
- Validation is less dependent on a specific global agent configuration.
- Hook installation remains opt-in through `git config core.hooksPath .githooks`.
