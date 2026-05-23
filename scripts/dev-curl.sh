#!/usr/bin/env bash
# Usage: ./scripts/dev-curl.sh <method> <path> [extra curl args]
# Examples:
#   ./scripts/dev-curl.sh GET /vendors/cafes
#   ./scripts/dev-curl.sh GET "/vendors?q=cafes&name=bean"
#   ./scripts/dev-curl.sh POST /vendors/cafes

set -e

METHOD=${1:?Usage: dev-curl.sh <METHOD> <path> [curl args]}
PATH=${2:?Usage: dev-curl.sh <METHOD> <path> [curl args]}
shift 2

BASE_URL="http://localhost:8080"
TOKEN=$(./gradlew :scripts:generateToken -q 2>/dev/null | tail -1)

curl -s -X "$METHOD" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  "${@}" \
  "$BASE_URL$PATH" | jq .
