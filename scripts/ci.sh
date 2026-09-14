#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$root_dir"

run_stage() {
  local stage_name="$1"
  shift
  echo "==> $stage_name"
  "$@"
}

run_stage "Validate specification structure" ./scripts/check-specs.sh
run_stage "Compile application and test sources" \
  ./mvnw --batch-mode --no-transfer-progress clean test-compile -DskipTests
run_stage "Run unit tests" \
  ./mvnw --batch-mode --no-transfer-progress -Dskip.integration.tests=true test

if find src/test -type f -name '*IT.java' -print -quit | grep -q .; then
  run_stage "Verify Docker for Testcontainers" docker info
else
  echo "==> No *IT.java tests require Docker"
fi

run_stage "Run integration tests" \
  ./mvnw --batch-mode --no-transfer-progress -Dskip.unit.tests=true verify
run_stage "Build application package" \
  ./mvnw --batch-mode --no-transfer-progress -DskipTests package
run_stage "Run static analysis" \
  ./mvnw --batch-mode --no-transfer-progress checkstyle:check
