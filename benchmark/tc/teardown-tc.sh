#!/usr/bin/env bash
set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "This script must be run as root (sudo)." >&2
  exit 1
fi

# Remove iptables marks (only the ones we added)
for port in 8080 8081 8082 4444; do
  iptables -t mangle -D OUTPUT -p tcp --dport "$port" -j MARK --set-mark 1 2>/dev/null || true
  iptables -t mangle -D INPUT  -p tcp --sport "$port" -j MARK --set-mark 1 2>/dev/null || true
done

# Remove tc from lo
tc qdisc del dev lo root 2>/dev/null && echo "tc qdisc removed from lo" || echo "No tc qdisc on lo"
