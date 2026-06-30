#!/bin/bash
OWNER_ID=1
PET_ID=1
HOST=http://localhost:8080
URL="$HOST/owners/$OWNER_ID/pets/new/db-bench"

echo "Checking if server is running at $HOST ..."

# Wait until server is up
until curl -fs "$HOST" >/dev/null 2>&1; do
    echo "Server not ready yet... retrying in 1s"
    sleep 1
done

echo "Server is up!"
echo "Running benchmark on: $URL"
echo "With Json payload { "nameNew": "leo", "birthDateNew": "2026-06-09", "typeNew": "bird"}"
echo "Warm up for 500 iterations"

ab -n 500 -c 1 -p pet.json -T application/json "$URL" > /dev/null
echo "Warm up finished"

echo "Start benchmark for 5000 sequential iterations"
ab -n 5000 -c 1 -p pet.json -T application/json "$URL" > results.txt

echo "Benchmark finished. Results saved to results.txt"