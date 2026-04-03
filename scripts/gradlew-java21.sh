#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="$ROOT_DIR/.tools/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"

exec "$ROOT_DIR/gradlew" "$@"
