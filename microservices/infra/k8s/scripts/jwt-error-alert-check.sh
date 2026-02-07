#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-resume}"
SINCE="${SINCE:-15m}"
DEPLOYMENTS="${DEPLOYMENTS:-resume-auth-service resume-profile-service resume-messaging-service}"
ALERT_PATTERN='jwt_validation_error=1 .*jwt_error_type=(invalid_signature|unknown_kid)'

log() {
  printf '[jwt-alert-check] %s\n' "$*"
}

for cmd in kubectl grep; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "Required command not found: ${cmd}" >&2
    exit 1
  fi
done

found_alert=0
for deployment in ${DEPLOYMENTS}; do
  if ! kubectl -n "${NAMESPACE}" get deployment "${deployment}" >/dev/null 2>&1; then
    log "Skipping missing deployment/${deployment}"
    continue
  fi
  log "Scanning deployment/${deployment} logs since ${SINCE}"
  logs="$(kubectl -n "${NAMESPACE}" logs "deployment/${deployment}" --all-containers=true --since="${SINCE}" 2>/dev/null || true)"
  matches="$(grep -E "${ALERT_PATTERN}" <<< "${logs}" || true)"
  if [[ -n "${matches}" ]]; then
    found_alert=1
    log "Detected JWT validation alerts in deployment/${deployment}:"
    echo "${matches}"
  fi
done

if [[ "${found_alert}" -eq 1 ]]; then
  echo "JWT validation alert conditions detected" >&2
  exit 1
fi

log "No invalid_signature/unknown_kid events detected."
