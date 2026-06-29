#!/usr/bin/env bash
set -euo pipefail

# Uses iptables + tc fw to constrain traffic on lo for ports 8080,8081,8082,4444.
# Chromedriver traffic (random ephemeral port) is unmarked and bypasses the delay.

PORTS=(8080 8081 8082 4444)
DELAY="${1:-600}"       # ms
JITTER="${2:-100}"      # ms
LOSS="${3:-5}"          # percent

if [[ $EUID -ne 0 ]]; then
  echo "This script must be run as root (sudo)." >&2
  exit 1
fi

# Remove any previous tc on lo
tc qdisc del dev lo root 2>/dev/null || true

# Remove stale iptables marks from previous runs
for port in 8080 8081 8082 4444; do
  iptables -t mangle -D OUTPUT -p tcp --dport "$port" -j MARK --set-mark 1 2>/dev/null || true
  iptables -t mangle -D INPUT -p tcp --sport "$port" -j MARK --set-mark 1 2>/dev/null || true
done

# Mark traffic to/from app ports with fwmark 1
for port in "${PORTS[@]}"; do
  iptables -t mangle -A OUTPUT -p tcp --dport "$port" -j MARK --set-mark 1
  iptables -t mangle -A INPUT -p tcp --sport "$port" -j MARK --set-mark 1
done

# prio qdisc on lo (bands 1:1, 1:2, 1:3)
tc qdisc replace dev lo root handle 1: prio

# netem on band 1:1
tc qdisc add dev lo parent 1:1 handle 10: netem delay "${DELAY}ms" "${JITTER}ms" loss "${LOSS}%"

# fw filter: marked packets → band 1:1 (delayed)
tc filter add dev lo parent 1: protocol ip prio 1 handle 1 fw flowid 1:1

echo "iptables + tc netem delay ${DELAY}ms ±${JITTER}ms loss ${LOSS}% applied on lo for ports: ${PORTS[*]}"
