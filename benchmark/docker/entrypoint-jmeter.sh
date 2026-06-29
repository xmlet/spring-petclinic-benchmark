#!/bin/bash
set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

wait_for_all_services

print_info "Starting benchmark..."
exec ./jmeter/run-jmeter.sh
