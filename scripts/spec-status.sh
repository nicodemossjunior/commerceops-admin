#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
specs_dir="$root_dir/specs"

printf '%-5s %-28s %-12s %s\n' "ID" "SPEC" "STATUS" "TASKS"

while IFS= read -r spec_dir; do
  spec_name="$(basename "$spec_dir")"
  spec_id="${spec_name%%-*}"
  title="${spec_name#???-}"
  task_file="$spec_dir/tasks.md"

  total=0
  done=0

  if [[ -f "$task_file" ]]; then
    total="$(grep -Ec '^- \[[ xX]\]' "$task_file" || true)"
    done="$(grep -Ec '^- \[[xX]\]' "$task_file" || true)"
  fi

  if [[ "$total" -eq 0 ]]; then
    status="unknown"
  elif [[ "$done" -eq "$total" ]]; then
    status="completed"
  elif [[ "$done" -eq 0 ]]; then
    status="planned"
  else
    status="in-progress"
  fi

  printf '%-5s %-28s %-12s %s/%s\n' "$spec_id" "$title" "$status" "$done" "$total"
done < <(find "$specs_dir" -mindepth 1 -maxdepth 1 -type d | sort)
