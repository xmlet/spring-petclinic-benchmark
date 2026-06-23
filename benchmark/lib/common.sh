#!/bin/bash
# Shared functions for benchmark scripts.
# Source this file: source "$(dirname "$0")/../lib/common.sh"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Wait for a TCP port to be open.
# Usage: wait_for <host> <port> <name> [timeout_seconds]
wait_for() {
    local host=$1 port=$2 name=$3 timeout=${4:-120}
    local elapsed=0
    while ! bash -c "echo >/dev/tcp/$host/$port" 2>/dev/null; do
        if [ "$elapsed" -ge "$timeout" ]; then
            print_error "$name not ready after ${timeout}s"
            return 1
        fi
        echo "  Waiting for $name on $host:$port..."
        sleep 5
        elapsed=$((elapsed + 5))
    done
    echo "  $name ready."
}

# Wait for all services to be ready (in parallel).
# Usage: wait_for_all_services
wait_for_all_services() {
    print_info "Waiting for services to be ready..."
    local pids=()
    wait_for localhost 8080 "Petclinic-Thymeleaf" 300 & pids+=($!)
    wait_for localhost 8081 "Petclinic-HtmlFlow-DataStar" 300 & pids+=($!)
    wait_for localhost 8082 "Petclinic-React-Backend" 300 & pids+=($!)
    wait_for localhost 4444 "Petclinic-React-Frontend" 300 & pids+=($!)
    local fail=0
    for pid in "${pids[@]}"; do
        wait "$pid" || fail=1
    done
    if [ "$fail" -ne 0 ]; then
        print_error "Some services failed to start."
        return 1
    fi
    print_info "All services ready."
}
