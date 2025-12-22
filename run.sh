#!/bin/bash
# BoneChild Run Script
# Handles platform-specific JVM arguments for macOS and debug mode
set -e
JAR_FILE="engine/target/bonechild-engine-1.0.0-all.jar"
# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first with: ./build.sh"
    exit 1
fi
# Debug options
DEBUG=false
SUSPEND="n"
PORT="5005"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --debug) DEBUG=true; SUSPEND="y"; shift ;;
    --nosuspend) SUSPEND="n"; shift ;;
    --port) PORT="${2:?missing port}"; shift 2 ;;
    *) echo "Unknown arg: $1" >&2; exit 2 ;;
  esac
done
JAVA_OPTS=()
if [[ "$DEBUG" == "true" ]]; then
  JAVA_OPTS+=("-agentlib:jdwp=transport=dt_socket,server=y,suspend=${SUSPEND},address=127.0.0.1:${PORT}")
fi
# Detect OS
OS="$(uname -s)"
case "$OS" in
    Darwin)
        java -XstartOnFirstThread "${JAVA_OPTS[@]}" -jar "$JAR_FILE"
        ;;
    Linux)
        java "${JAVA_OPTS[@]}" -jar "$JAR_FILE"
        ;;
    *)
        java "${JAVA_OPTS[@]}" -jar "$JAR_FILE"
        ;;
esac
