#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
checker="$root_dir/scripts/check-docs.sh"
test_dir="$(mktemp -d "${TMPDIR:-/tmp}/commerceops-docs-test.XXXXXX")"
trap 'rm -rf "$test_dir"' EXIT

create_fixture() {
  local fixture_dir="$1"

  mkdir -p "$fixture_dir/docs" "$fixture_dir/specs/000-foundation" "$fixture_dir/specs/001-next"

  printf '%s\n' \
    '# Fixture' \
    '' \
    '[Guide](docs/guide.md)' \
    '' \
    '`SERVER_PORT`' \
    '' \
    'All specifications `000` through `000` are complete.' \
    > "$fixture_dir/README.md"

  printf '%s\n' 'SERVER_PORT=8080' > "$fixture_dir/.env.example"
  printf '%s\n' '# Guide' > "$fixture_dir/docs/guide.md"
  printf '%s\n' \
    '# Specifications' \
    '' \
    '| ID | Specification | Status | Purpose |' \
    '| --- | --- | --- | --- |' \
    '| 000 | [Foundation](000-foundation/spec.md) | completed | Foundation. |' \
    '| 001 | [Next](001-next/spec.md) | planned | Next increment. |' \
    > "$fixture_dir/specs/README.md"

  printf '%s\n' '# Spec: Foundation' 'status: completed' > "$fixture_dir/specs/000-foundation/spec.md"
  printf '%s\n' '# Tasks' '- [x] Complete foundation.' > "$fixture_dir/specs/000-foundation/tasks.md"
  printf '%s\n' '# Spec: Next' 'status: planned' > "$fixture_dir/specs/001-next/spec.md"
  printf '%s\n' '# Tasks' '- [ ] Implement next increment.' > "$fixture_dir/specs/001-next/tasks.md"
}

assert_failure() {
  local name="$1"
  local expected_message="$2"
  local fixture_dir="$test_dir/$name"
  local output

  create_fixture "$fixture_dir"
  "$name" "$fixture_dir"

  if output="$($checker "$fixture_dir" 2>&1)"; then
    echo "Expected '$name' to fail." >&2
    exit 1
  fi

  if [[ "$output" != *"$expected_message"* ]]; then
    echo "Failure '$name' did not contain: $expected_message" >&2
    echo "$output" >&2
    exit 1
  fi
}

valid_fixture="$test_dir/valid"
create_fixture "$valid_fixture"
"$checker" "$valid_fixture" >/dev/null

broken_link() {
  rm "$1/docs/guide.md"
}
assert_failure "broken_link" "links to missing target 'docs/guide.md'"

missing_spec() {
  sed '/| 001 |/d' "$1/specs/README.md" > "$1/specs/README.tmp"
  mv "$1/specs/README.tmp" "$1/specs/README.md"
}
assert_failure "missing_spec" "Spec 001 (001-next) is missing"

incorrect_status() {
  sed 's/| completed | Foundation/| planned | Foundation/' "$1/specs/README.md" > "$1/specs/README.tmp"
  mv "$1/specs/README.tmp" "$1/specs/README.md"
}
assert_failure "incorrect_status" "Spec 000 index status is 'planned'; tasks require 'completed'"

undocumented_variable() {
  printf '%s\n' 'FEATURE_FLAG=true' >> "$1/.env.example"
}
assert_failure "undocumented_variable" "Environment variable 'FEATURE_FLAG'"

stale_range() {
  sed 's/through `000`/through `099`/' "$1/README.md" > "$1/README.tmp"
  mv "$1/README.tmp" "$1/README.md"
}
assert_failure "stale_range" "completed specification range as 000 through 000"

echo "Documentation checker tests passed."
