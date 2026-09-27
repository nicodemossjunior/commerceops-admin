#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$root_dir"

./scripts/check-specs.sh
./scripts/test-check-docs.sh
./scripts/check-docs.sh
./mvnw test
