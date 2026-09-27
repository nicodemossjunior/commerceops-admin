#!/usr/bin/env bash
set -euo pipefail
root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root_dir"
if [[ $# -eq 0 ]]; then
  echo "Usage: ./scripts/demo.sh load [--seed N] [--reference-time UTC] | inspect | reset" >&2
  exit 1
fi
export SPRING_PROFILES_ACTIVE=demo
./mvnw --quiet -DskipTests compile dependency:build-classpath -Dmdep.outputFile=target/demo-classpath.txt
exec java -cp "target/classes:$(cat target/demo-classpath.txt)" com.commerceops.admin.demo.DemoCommand "$@"
