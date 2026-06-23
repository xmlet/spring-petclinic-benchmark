#!/bin/bash
# Runs the JMeter benchmark.
# Expects services to already be running.
#
# Usage: ./jmeter/run-jmeter.sh

set -eo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

source "$SCRIPT_DIR/../lib/common.sh"

# ========================================
# Configuration
# ========================================
OWNER_ID=1
TEST_THREADS=1
TEST_LOOPS=250
WARMUP_LOOPS=10
OUTPUT_DIR="../results"

# ========================================
# Pre-flight checks
# ========================================
if ! command -v jmeter &> /dev/null; then
    print_error "JMeter is not installed or not on PATH."
    exit 1
fi

if ! command -v google-chrome &> /dev/null && ! command -v chromium-browser &> /dev/null && ! command -v chromium &> /dev/null; then
    print_error "Chrome/Chromium is not installed."
    exit 1
fi

# ========================================
# Warm-up Phase
# ========================================
if [ -z "$REPORT_DIR" ]; then
    TS="${TIMESTAMP:-$(date +"%Y%m%d_%H%M%S")}"
    REPORT_DIR="$OUTPUT_DIR/report_${TS}"
fi
RESULTS_FILE="$REPORT_DIR/results.jtl"
LOG_FILE="$REPORT_DIR/jmeter.log"
HTML_DIR="$REPORT_DIR/report"

mkdir -p "$REPORT_DIR"

print_info "Running warm-up ($WARMUP_LOOPS iterations)..."

WARMUP_LOG="$REPORT_DIR/warmup.log"
jmeter -n -t petclinic_create_pet_webdriver.jmx \
    -Jjmeter.reportgenerator.enabled=false \
    -JOWNER_ID=$OWNER_ID \
    -JTEST_THREADS=$TEST_THREADS \
    -JTEST_LOOPS=$WARMUP_LOOPS \
    -l /dev/null \
    -j "$WARMUP_LOG" \
    2>&1 | tee -a "$WARMUP_LOG"

print_info "Warm-up complete."

# ========================================
# Main Benchmark
# ========================================
print_info "Running benchmark..."
jmeter -n -t petclinic_create_pet_webdriver.jmx \
    -Jjmeter.reportgenerator.enabled=true \
    -JOWNER_ID=$OWNER_ID \
    -JTEST_THREADS=$TEST_THREADS \
    -JTEST_LOOPS=$TEST_LOOPS \
    -l "$RESULTS_FILE" \
    -j "$LOG_FILE" \
    2>&1 | tee -a "$LOG_FILE"

if [ -s "$RESULTS_FILE" ] && [ "$(wc -l < "$RESULTS_FILE")" -gt 1 ]; then
    print_info "Generating HTML report..."
    mkdir -p "$HTML_DIR"
    jmeter -g "$RESULTS_FILE" -o "$HTML_DIR" 2>&1 | tee -a "$LOG_FILE"
    print_info "HTML Report: $HTML_DIR/index.html"
fi

print_info "Done. Results: $REPORT_DIR"
