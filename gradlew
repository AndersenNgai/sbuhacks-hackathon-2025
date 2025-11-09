#!/bin/sh

# Minimal Gradle wrapper bootstrap script.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ -n "${JAVA_HOME:-}" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="$(command -v java || true)"
fi

if [ ! -x "${JAVA_CMD:-}" ]; then
  echo "ERROR: Java runtime could not be located. Set JAVA_HOME or ensure java is on PATH." >&2
  exit 1
fi

exec "$JAVA_CMD" -Xmx64m -Xms64m -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
