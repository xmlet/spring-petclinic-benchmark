#!/bin/bash
# Unified benchmark runner.
#
# Usage:
#   ./run-benchmark.sh --docker          Run via Docker containers
#   ./run-benchmark.sh --local           Run locally (default)

set -eo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

source "$SCRIPT_DIR/lib/common.sh"

# ========================================
# Parse arguments
# ========================================
MODE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --docker) MODE="docker"; shift ;;
        --local) MODE="local"; shift ;;
        *) print_error "Unknown option: $1"; exit 1 ;;
    esac
done

# Default to local if no mode specified
MODE="${MODE:-local}"

# ========================================
# Docker mode
# ========================================
run_docker() {
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    export TIMESTAMP
    REPORT_DIR="./results/report_${TIMESTAMP}"

    docker_cleanup() {
        print_info "Cleaning up Docker containers..."
        docker compose down -v 2>/dev/null || true
    }
    trap docker_cleanup EXIT SIGINT SIGTERM

    docker compose down -v 2>/dev/null || true

    print_info "Starting all services via Docker..."
    docker compose up -d

    print_info "Following JMeter output (Ctrl+C to stop)..."
    docker compose logs -f jmeter || true

    mkdir -p "$REPORT_DIR/server_logs"
    print_info "Saving server logs..."
    docker compose logs petclinic-thymeleaf > "$REPORT_DIR/server_logs/thymeleaf.log" 2>&1
    docker compose logs petclinic-htmlflow-datastar > "$REPORT_DIR/server_logs/htmlflow-datastar.log" 2>&1
    docker compose logs petclinic-react-backend > "$REPORT_DIR/server_logs/react-backend.log" 2>&1
    docker compose logs petclinic-react-frontend > "$REPORT_DIR/server_logs/react-frontend.log" 2>&1

    print_info "Extracting benchmark results..."
    docker compose cp jmeter:/benchmark/results/report_${TIMESTAMP}/. "$REPORT_DIR/"

    docker compose down -v

    print_info "Done. Results in: $REPORT_DIR"
}

# ========================================
# Local mode
# ========================================
PIDS=()

start_servers() {
    LOG_DIR="$REPORT_DIR/server_logs"

    print_info "Starting petclinic-thymeleaf on port 8080..."
    "$SCRIPT_DIR/../petclinic-thymeleaf/gradlew" -p "$SCRIPT_DIR/../petclinic-thymeleaf" bootRun --no-daemon > "$LOG_DIR/thymeleaf.log" 2>&1 &
    PIDS+=($!)

    print_info "Starting petclinic-htmlflow-datastar on port 8081..."
    "$SCRIPT_DIR/../petclinic-htmlflow-datastar/gradlew" -p "$SCRIPT_DIR/../petclinic-htmlflow-datastar" bootRun --args='--server.port=8081' --no-daemon > "$LOG_DIR/htmlflow-datastar.log" 2>&1 &
    PIDS+=($!)

    print_info "Starting petclinic-react backend on port 8082..."
    cd "$SCRIPT_DIR/../petclinic-react"
    ./mvnw spring-boot:run -Dspring-boot.run.arguments='--server.port=8082' > "$LOG_DIR/react-backend.log" 2>&1 &
    PIDS+=($!)
    cd "$SCRIPT_DIR"

    print_info "Starting petclinic-react frontend on port 4444..."
    cd "$SCRIPT_DIR/../petclinic-react/client"
    PORT=4444 npm start > "$LOG_DIR/react-frontend.log" 2>&1 &
    PIDS+=($!)
    cd "$SCRIPT_DIR"
}

stop_servers() {
    print_info "Shutting down all services..."
    for pid in "${PIDS[@]}"; do
        kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
    print_info "All services stopped."
}

run_local() {
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    export REPORT_DIR="$SCRIPT_DIR/results/report_${TIMESTAMP}"
    mkdir -p "$REPORT_DIR/server_logs"

    trap stop_servers EXIT SIGINT SIGTERM

    start_servers
    wait_for_all_services

    print_info "Running benchmark..."
    if ! ./jmeter/run-jmeter.sh; then
        print_error "Benchmark failed. Check logs in $REPORT_DIR"
        exit 1
    fi

    print_info "Benchmark complete."
}

# ========================================
# Main
# ========================================
case "$MODE" in
    docker) run_docker ;;
    local)  run_local ;;
    *)      print_error "Invalid mode: $MODE"; exit 1 ;;
esac
