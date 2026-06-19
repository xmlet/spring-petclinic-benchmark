#!/bin/bash
# Builds all Petclinic implementations and installs React frontend dependencies.
#
# Usage: ./build-all.sh

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Clean previous server logs
rm -rf "$SCRIPT_DIR/results/server_logs"

echo "Building petclinic-thymeleaf..."
"$SCRIPT_DIR/../petclinic-thymeleaf/gradlew" -p "$SCRIPT_DIR/../petclinic-thymeleaf" build --no-daemon

echo "Building petclinic-htmlflow-datastar..."
"$SCRIPT_DIR/../petclinic-htmlflow-datastar/gradlew" -p "$SCRIPT_DIR/../petclinic-htmlflow-datastar" build --no-daemon

echo "Building petclinic-react backend..."
cd "$SCRIPT_DIR/../petclinic-react"
./mvnw clean package -q

echo "Installing petclinic-react frontend dependencies..."
cd "$SCRIPT_DIR/../petclinic-react/client"
npm install --legacy-peer-deps
cd "$SCRIPT_DIR"

echo "All implementations built successfully."
