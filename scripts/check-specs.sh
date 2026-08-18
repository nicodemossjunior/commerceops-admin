#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
specs_dir="$root_dir/specs"

if [[ ! -f "$specs_dir/README.md" ]]; then
  echo "Missing specs/README.md" >&2
  exit 1
fi

status=0

while IFS= read -r spec_dir; do
  spec_name="$(basename "$spec_dir")"

  if [[ ! "$spec_name" =~ ^[0-9]{3}-[a-z0-9-]+$ ]]; then
    echo "Invalid spec directory name: specs/$spec_name" >&2
    status=1
  fi

  if [[ ! -f "$spec_dir/spec.md" ]]; then
    echo "Missing specs/$spec_name/spec.md" >&2
    status=1
  fi

  if [[ ! -f "$spec_dir/tasks.md" ]]; then
    echo "Missing specs/$spec_name/tasks.md" >&2
    status=1
  fi

  if [[ -f "$spec_dir/spec.md" ]] && ! grep -q '^## Acceptance Criteria' "$spec_dir/spec.md"; then
    echo "Missing Acceptance Criteria section in specs/$spec_name/spec.md" >&2
    status=1
  fi

  if [[ -f "$spec_dir/spec.md" ]] && ! grep -q '^## Expected Tests' "$spec_dir/spec.md"; then
    echo "Missing Expected Tests section in specs/$spec_name/spec.md" >&2
    status=1
  fi

  if [[ -f "$spec_dir/tasks.md" ]] && ! grep -q '^## Validation' "$spec_dir/tasks.md"; then
    echo "Missing Validation section in specs/$spec_name/tasks.md" >&2
    status=1
  fi
done < <(find "$specs_dir" -mindepth 1 -maxdepth 1 -type d | sort)

if [[ "$status" -ne 0 ]]; then
  exit "$status"
fi

echo "Spec structure is valid."
