#!/bin/bash
# Runs the Create Pet benchmark across all 3 Petclinic implementations.
#
# Usage: ./petclinic_benchmark_runner.sh [--headless]

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ========================================
# Configuration — edit these to change run settings
# ========================================
PORTS_THYMELEAF=8080
PORTS_HTMLFLOW=8081
PORTS_REACT=8082
REACT_FRONTEND_PORT=4444
OWNER_ID=1

# Test configurations
TEST_THREADS=1
TEST_LOOPS=250
WARMUP_LOOPS=10

# Output
OUTPUT_DIR="./results"

# ========================================
# Parse arguments
# ========================================
HEADLESS=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --headless) HEADLESS=true; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

# ========================================
# Color output
# ========================================
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ========================================
# Pre-flight checks
# ========================================
if ! command -v google-chrome &> /dev/null && ! command -v chromium-browser &> /dev/null && ! command -v chromium &> /dev/null; then
    print_error "Chrome/Chromium is not installed."
    exit 1
fi

check_service() {
    if ! timeout 5 bash -c "echo >/dev/tcp/$1/$2" 2>/dev/null; then
        print_error "$3 is NOT running on $1:$2"
        return 1
    fi
}

print_info "Checking services..."
SERVICES_OK=true
check_service localhost $PORTS_THYMELEAF "Thymeleaf" || SERVICES_OK=false
check_service localhost $PORTS_HTMLFLOW "HtmlFlow-DataStar" || SERVICES_OK=false
check_service localhost $PORTS_REACT "React Backend" || SERVICES_OK=false
check_service localhost $REACT_FRONTEND_PORT "React Frontend" || SERVICES_OK=false

if [ "$SERVICES_OK" = false ]; then
    print_error "Not all services are running!"
    exit 1
fi
print_info "All services running."

# ========================================
# Run JMeter — Warm-up Phase
# ========================================
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_DIR="$OUTPUT_DIR/report_${TIMESTAMP}"
RESULTS_FILE="$REPORT_DIR/results.jtl"
LOG_FILE="$REPORT_DIR/jmeter.log"
HTML_DIR="$REPORT_DIR/report"

mkdir -p "$REPORT_DIR"

print_info "Running warm-up ($WARMUP_LOOPS iterations)..."

WARMUP_LOG="$REPORT_DIR/warmup.log"
WARMUP_JMETER_CMD="jmeter -n -t jmeter/petclinic_create_pet_webdriver.jmx \
    -Jjmeter.reportgenerator.enabled=false \
    -JOWNER_ID=$OWNER_ID \
    -JTEST_THREADS=$TEST_THREADS \
    -JTEST_LOOPS=$WARMUP_LOOPS \
    -l /dev/null \
    -j $WARMUP_LOG"

eval $WARMUP_JMETER_CMD
print_info "Warm-up complete."

# ========================================
# Run JMeter — Main Benchmark
# ========================================
JMETER_CMD="jmeter"
[ "$HEADLESS" = true ] && JMETER_CMD="$JMETER_CMD -n"

JMETER_CMD="$JMETER_CMD -t jmeter/petclinic_create_pet_webdriver.jmx \
    -Jjmeter.reportgenerator.enabled=true \
    -JOWNER_ID=$OWNER_ID \
    -JTEST_THREADS=$TEST_THREADS \
    -JTEST_LOOPS=$TEST_LOOPS \
    -l $RESULTS_FILE \
    -j $LOG_FILE"

if [ "$HEADLESS" = true ]; then
    mkdir -p "$HTML_DIR"
    JMETER_CMD="$JMETER_CMD -e -o $HTML_DIR"
fi

print_info "Running benchmark..."
eval $JMETER_CMD

print_info "Done. Results: $REPORT_DIR"
[ "$HEADLESS" = true ] && print_info "HTML Report: $HTML_DIR/index.html"
