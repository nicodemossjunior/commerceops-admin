#!/usr/bin/env bash
set -euo pipefail

default_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
root_dir="${1:-$default_root}"
root_dir="$(cd "$root_dir" && pwd)"

status=0

report_error() {
  echo "Documentation error: $*" >&2
  status=1
}

check_local_links() {
  local markdown_file
  local source_dir
  local record
  local line_number
  local expression
  local target

  while IFS= read -r markdown_file; do
    source_dir="$(dirname "$markdown_file")"

    while IFS= read -r record; do
      line_number="${record%%:*}"
      expression="${record#*:}"
      target="${expression#](}"
      target="${target%)}"
      target="${target%% \"*}"
      target="${target#<}"
      target="${target%>}"

      case "$target" in
        ""|\#*|/*|*://*|mailto:*|tel:*|data:*)
          continue
          ;;
      esac

      target="${target%%#*}"
      target="${target%%\?*}"

      if [[ ! -e "$source_dir/$target" ]]; then
        report_error "${markdown_file#"$root_dir"/}:$line_number links to missing target '$target'."
      fi
    done < <(grep -nEo '\]\([^)]*\)' "$markdown_file" || true)
  done < <(find "$root_dir" \
    -type d \( -name .git -o -name target -o -name node_modules \) -prune -o \
    -type f -name '*.md' -print | sort)
}

task_status() {
  local task_file="$1"
  local total
  local done

  total="$(grep -Ec '^- \[[ xX]\]' "$task_file" || true)"
  done="$(grep -Ec '^- \[[xX]\]' "$task_file" || true)"

  if [[ "$total" -eq 0 ]]; then
    echo "unknown"
  elif [[ "$done" -eq "$total" ]]; then
    echo "completed"
  elif [[ "$done" -eq 0 ]]; then
    echo "planned"
  else
    echo "in-progress"
  fi
}

check_spec_index() {
  local specs_dir="$root_dir/specs"
  local index_file="$specs_dir/README.md"
  local spec_dir
  local spec_name
  local spec_id
  local index_row
  local indexed_status
  local expected_status

  if [[ ! -f "$index_file" ]]; then
    report_error "Missing specs/README.md."
    return
  fi

  while IFS= read -r spec_dir; do
    spec_name="$(basename "$spec_dir")"
    spec_id="${spec_name%%-*}"

    if [[ ! -f "$spec_dir/spec.md" || ! -f "$spec_dir/tasks.md" ]]; then
      continue
    fi

    index_row="$(grep -E "^\| $spec_id \|.*\]\($spec_name/spec\.md\) \|" "$index_file" || true)"
    if [[ -z "$index_row" ]]; then
      report_error "Spec $spec_id ($spec_name) is missing from specs/README.md or links to the wrong path."
      continue
    fi

    expected_status="$(task_status "$spec_dir/tasks.md")"
    indexed_status="$(printf '%s\n' "$index_row" | awk -F '|' '{ value=$4; gsub(/^[[:space:]]+|[[:space:]]+$/, "", value); print value }')"
    if [[ "$indexed_status" != "$expected_status" ]]; then
      report_error "Spec $spec_id index status is '$indexed_status'; tasks require '$expected_status'."
    fi
  done < <(find "$specs_dir" -mindepth 1 -maxdepth 1 -type d -name '[0-9][0-9][0-9]-*' | sort)
}

check_environment_reference() {
  local env_file="$root_dir/.env.example"
  local readme_file="$root_dir/README.md"
  local line
  local variable

  if [[ ! -f "$env_file" ]]; then
    report_error "Missing .env.example."
    return
  fi

  if [[ ! -f "$readme_file" ]]; then
    report_error "Missing README.md."
    return
  fi

  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" == \#* || "$line" != *=* ]] && continue
    variable="${line%%=*}"

    if ! grep -Fq "\`$variable\`" "$readme_file"; then
      report_error "Environment variable '$variable' from .env.example is not documented in README.md."
    fi
  done < "$env_file"
}

check_completed_spec_range() {
  local specs_dir="$root_dir/specs"
  local readme_file="$root_dir/README.md"
  local spec_dir
  local expected_status
  local latest_completed=""

  [[ -f "$readme_file" ]] || return

  while IFS= read -r spec_dir; do
    [[ -f "$spec_dir/tasks.md" ]] || continue
    expected_status="$(task_status "$spec_dir/tasks.md")"
    if [[ "$expected_status" == "completed" ]]; then
      latest_completed="$(basename "$spec_dir")"
      latest_completed="${latest_completed%%-*}"
    fi
  done < <(find "$specs_dir" -mindepth 1 -maxdepth 1 -type d -name '[0-9][0-9][0-9]-*' | sort)

  if [[ -n "$latest_completed" ]] && \
    ! grep -Fq "All specifications \`000\` through \`$latest_completed\` are complete." "$readme_file"; then
    report_error "README.md must report the completed specification range as 000 through $latest_completed."
  fi
}

check_local_links
check_spec_index
check_environment_reference
check_completed_spec_range

if [[ "$status" -ne 0 ]]; then
  exit "$status"
fi

echo "Documentation consistency is valid."
