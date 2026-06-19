#!/bin/bash
# Starts all Petclinic services in the background.
# Keeps the terminal open — Ctrl+C kills all services.
#
# Usage: ./start-all.sh

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/results/server_logs"
mkdir -p "$LOG_DIR"

PIDS=()

cleanup() {
    echo ""
    echo "Shutting down all services..."
    for pid in "${PIDS[@]}"; do
        kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
    echo "All services stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "Starting petclinic-thymeleaf on port 8080..."
"$SCRIPT_DIR/../petclinic-thymeleaf/gradlew" -p "$SCRIPT_DIR/../petclinic-thymeleaf" bootRun --no-daemon > "$LOG_DIR/thymeleaf.log" 2>&1 &
PIDS+=($!)
echo "petclinic-thymeleaf process: $!"

echo "Starting petclinic-htmlflow-datastar on port 8081..."
"$SCRIPT_DIR/../petclinic-htmlflow-datastar/gradlew" -p "$SCRIPT_DIR/../petclinic-htmlflow-datastar" bootRun --args='--server.port=8081' --no-daemon > "$LOG_DIR/htmlflow-datastar.log" 2>&1 &
PIDS+=($!)
echo "petclinic-htmlflow-datastar process: $!"

echo "Starting petclinic-react backend on port 8082..."
cd "$SCRIPT_DIR/../petclinic-react"
./mvnw spring-boot:run -Dspring-boot.run.arguments='--server.port=8082' > "$LOG_DIR/react-backend.log" 2>&1 &
PIDS+=($!)
echo "petclinic-react backend process: $!"
cd "$SCRIPT_DIR"

echo "Starting petclinic-react frontend on port 4444..."
cd "$SCRIPT_DIR/../petclinic-react/client"
PORT=4444 npm start > "$LOG_DIR/react-frontend.log" 2>&1 &
PIDS+=($!)
echo "petclinic-react frontend process: $!"
cd "$SCRIPT_DIR"

echo ""
echo "All services running. Waiting for Ctrl+C to stop..."
echo "  Logs: $LOG_DIR/*.log"

wait
