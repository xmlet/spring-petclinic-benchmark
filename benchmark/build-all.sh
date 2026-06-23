#!/bin/bash
# Builds all Petclinic implementations and installs React frontend dependencies.
#
# Usage: ./build-all.sh

set -eo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

source "$SCRIPT_DIR/lib/common.sh"

print_info "Building petclinic-thymeleaf..."
"$SCRIPT_DIR/../petclinic-thymeleaf/gradlew" -p "$SCRIPT_DIR/../petclinic-thymeleaf" build --no-daemon

print_info "Building petclinic-htmlflow-datastar..."
"$SCRIPT_DIR/../petclinic-htmlflow-datastar/gradlew" -p "$SCRIPT_DIR/../petclinic-htmlflow-datastar" build --no-daemon

print_info "Building petclinic-react backend..."
cd "$SCRIPT_DIR/../petclinic-react"
./mvnw clean package

print_info "Installing petclinic-react frontend dependencies..."
cd "$SCRIPT_DIR/../petclinic-react/client"
npm install --legacy-peer-deps
cd "$SCRIPT_DIR"

print_info "All implementations built successfully."
