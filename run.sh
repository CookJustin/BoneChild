#!/bin/bash
# BoneChild Run Script
# Handles platform-specific JVM arguments for macOS
set -e
JAR_FILE="engine/target/bonechild-engine-1.0.0-all.jar"
# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first with: ./build.sh"
    exit 1
fi
# Detect OS
OS="$(uname -s)"
case "$OS" in
    Darwin)
        echo "Running on macOS with -XstartOnFirstThread flag..."
        java -XstartOnFirstThread -jar "$JAR_FILE"
        ;;
    Linux)
        echo "Running on Linux..."
        java -jar "$JAR_FILE"
        ;;
    *)
        echo "Running on $OS..."
        java -jar "$JAR_FILE"
        ;;
esac
