#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-resume}"
GATEWAY_SERVICE="${GATEWAY_SERVICE:-resume-gateway}"
GATEWAY_PORT="${GATEWAY_PORT:-8080}"
LOCAL_GATEWAY_PORT="${LOCAL_GATEWAY_PORT:-18080}"
AUTH_SELECTOR="${AUTH_SELECTOR:-app=resume-auth-service}"
AUTH_CONTAINER_PORT="${AUTH_CONTAINER_PORT:-8081}"
LOCAL_AUTH_PORT="${LOCAL_AUTH_PORT:-18081}"
CLIENT_ID="${CLIENT_ID:-resume-spa}"
REDIRECT_URI="${REDIRECT_URI:-http://localhost:4200/auth/callback}"
OIDC_SCOPE="${OIDC_SCOPE:-openid profile offline_access}"
SMOKE_USERNAME="${SMOKE_USERNAME:-}"
SMOKE_PASSWORD="${SMOKE_PASSWORD:-}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-300}"
HTTP_TIMEOUT_SECONDS="${HTTP_TIMEOUT_SECONDS:-15}"

if [[ -z "${SMOKE_USERNAME}" || -z "${SMOKE_PASSWORD}" ]]; then
  echo "SMOKE_USERNAME and SMOKE_PASSWORD are required" >&2
  exit 1
fi

for cmd in kubectl curl jq openssl awk sed grep; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "Required command not found: ${cmd}" >&2
    exit 1
  fi
done

tmp_dir="$(mktemp -d)"
cookie_jar="${tmp_dir}/cookies.txt"
authorize_headers="${tmp_dir}/authorize.headers"
login_html="${tmp_dir}/login.html"
login_headers="${tmp_dir}/login.headers"
callback_headers="${tmp_dir}/callback.headers"
token_response="${tmp_dir}/token.json"
gateway_pf_log="${tmp_dir}/gateway-port-forward.log"
auth_pf_log="${tmp_dir}/auth-port-forward.log"
gateway_pf_pid=""
auth_pf_pid=""

cleanup() {
  if [[ -n "${auth_pf_pid}" ]] && kill -0 "${auth_pf_pid}" >/dev/null 2>&1; then
    kill "${auth_pf_pid}" >/dev/null 2>&1 || true
    wait "${auth_pf_pid}" >/dev/null 2>&1 || true
  fi
  if [[ -n "${gateway_pf_pid}" ]] && kill -0 "${gateway_pf_pid}" >/dev/null 2>&1; then
    kill "${gateway_pf_pid}" >/dev/null 2>&1 || true
    wait "${gateway_pf_pid}" >/dev/null 2>&1 || true
  fi
  rm -rf "${tmp_dir}"
}
trap cleanup EXIT

log() {
  printf '[oidc-smoke] %s\n' "$*"
}

urlencode() {
  jq -nr --arg value "$1" '$value|@uri'
}

urldecode() {
  local value="${1//+/ }"
  printf '%b' "${value//%/\\x}"
}

absolute_url() {
  local base_url="$1"
  local maybe_relative="$2"
  if [[ "${maybe_relative}" =~ ^https?:// ]]; then
    printf '%s\n' "${maybe_relative}"
  else
    printf '%s%s\n' "${base_url}" "${maybe_relative}"
  fi
}

extract_header() {
  local file="$1"
  local header_name="$2"
  awk -v header_name="${header_name}" '
    BEGIN { IGNORECASE = 1 }
    $1 ~ "^" header_name ":" {
      sub("\r$", "", $2);
      print $2;
      exit;
    }' "${file}"
}

extract_query_param() {
  local url="$1"
  local key="$2"
  sed -n "s/.*[?&]${key}=\([^&]*\).*/\1/p" <<< "${url}" | head -n 1
}

wait_http_ok() {
  local url="$1"
  local timeout="${2}"
  local started_at
  started_at="$(date +%s)"
  while true; do
    if curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" "${url}" >/dev/null 2>&1; then
      return 0
    fi
    if (( "$(date +%s)" - started_at > timeout )); then
      echo "Timeout waiting for ${url}" >&2
      return 1
    fi
    sleep 1
  done
}

restart_auth_pod() {
  local pod_to_restart
  pod_to_restart="$(kubectl -n "${NAMESPACE}" get pods -l "${AUTH_SELECTOR}" -o jsonpath='{.items[0].metadata.name}')"
  if [[ -z "${pod_to_restart}" ]]; then
    echo "No auth pod found with selector: ${AUTH_SELECTOR}" >&2
    return 1
  fi
  log "Restarting auth pod: ${pod_to_restart}"
  kubectl -n "${NAMESPACE}" delete pod "${pod_to_restart}" --wait=false >/dev/null
  kubectl -n "${NAMESPACE}" wait --for=condition=Ready pod -l "${AUTH_SELECTOR}" --timeout="${WAIT_TIMEOUT_SECONDS}s" >/dev/null
  local restarted_pod
  restarted_pod="$(kubectl -n "${NAMESPACE}" get pods -l "${AUTH_SELECTOR}" --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1:].metadata.name}')"
  if [[ -z "${restarted_pod}" ]]; then
    echo "Unable to resolve restarted auth pod" >&2
    return 1
  fi
  printf '%s\n' "${restarted_pod}"
}

log "Port-forwarding gateway service ${GATEWAY_SERVICE}:${GATEWAY_PORT} -> localhost:${LOCAL_GATEWAY_PORT}"
kubectl -n "${NAMESPACE}" port-forward "svc/${GATEWAY_SERVICE}" "${LOCAL_GATEWAY_PORT}:${GATEWAY_PORT}" >"${gateway_pf_log}" 2>&1 &
gateway_pf_pid=$!
wait_http_ok "http://127.0.0.1:${LOCAL_GATEWAY_PORT}/health" "${WAIT_TIMEOUT_SECONDS}"
gateway_base_url="http://127.0.0.1:${LOCAL_GATEWAY_PORT}"

code_verifier="$(openssl rand -base64 72 | tr '+/' '-_' | tr -d '=[:space:]' | cut -c1-96)"
code_challenge="$(printf '%s' "${code_verifier}" | openssl dgst -binary -sha256 | openssl base64 -A | tr '+/' '-_' | tr -d '=')"
state="$(openssl rand -hex 16)"
nonce="$(openssl rand -hex 16)"

authorize_url="${gateway_base_url}/oauth2/authorize?response_type=code&client_id=$(urlencode "${CLIENT_ID}")&redirect_uri=$(urlencode "${REDIRECT_URI}")&scope=$(urlencode "${OIDC_SCOPE}")&code_challenge=$(urlencode "${code_challenge}")&code_challenge_method=S256&state=$(urlencode "${state}")&nonce=$(urlencode "${nonce}")"

log "Step 1/6: signinRedirect (authorize)"
curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -D "${authorize_headers}" -o /dev/null \
  --cookie-jar "${cookie_jar}" \
  "${authorize_url}"
login_location="$(extract_header "${authorize_headers}" "Location")"
if [[ -z "${login_location}" ]]; then
  echo "Missing login redirect in authorize response" >&2
  exit 1
fi
login_url="$(absolute_url "${gateway_base_url}" "${login_location}")"
log "Authorize redirected to login: ${login_url}"

log "Step 2/6: login form submit"
curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -o "${login_html}" \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  "${login_url}"

csrf_token="$(sed -n 's/.*name="_csrf" value="\([^"]*\)".*/\1/p' "${login_html}" | head -n 1)"
if [[ -z "${csrf_token}" ]]; then
  echo "CSRF token not found on login page" >&2
  exit 1
fi

login_action="$(sed -n 's/.*<form[^>]*action="\([^"]*\)".*/\1/p' "${login_html}" | head -n 1)"
if [[ -z "${login_action}" ]]; then
  login_action="/login"
fi
login_action_url="$(absolute_url "${gateway_base_url}" "${login_action}")"

login_payload="username=$(urlencode "${SMOKE_USERNAME}")&password=$(urlencode "${SMOKE_PASSWORD}")&_csrf=$(urlencode "${csrf_token}")"
curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -D "${login_headers}" -o /dev/null \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data "${login_payload}" \
  "${login_action_url}"

login_redirect="$(extract_header "${login_headers}" "Location")"
if [[ -z "${login_redirect}" ]]; then
  echo "Login did not produce redirect (credentials may be invalid)" >&2
  exit 1
fi

log "Step 3/6: callback redirect with authorization code"
curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -D "${callback_headers}" -o /dev/null \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  "${authorize_url}"
callback_location="$(extract_header "${callback_headers}" "Location")"
if [[ -z "${callback_location}" ]]; then
  echo "Missing callback redirect after login" >&2
  exit 1
fi
if [[ "${callback_location}" != "${REDIRECT_URI}"* ]]; then
  echo "Unexpected callback redirect: ${callback_location}" >&2
  exit 1
fi

returned_state="$(urldecode "$(extract_query_param "${callback_location}" "state")")"
if [[ "${returned_state}" != "${state}" ]]; then
  echo "State mismatch in callback redirect" >&2
  exit 1
fi

authorization_code_encoded="$(extract_query_param "${callback_location}" "code")"
if [[ -z "${authorization_code_encoded}" ]]; then
  echo "Authorization code not found in callback redirect" >&2
  exit 1
fi
authorization_code="$(urldecode "${authorization_code_encoded}")"
log "Callback captured successfully: ${callback_location}"

log "Step 4/6: token exchange (authorization_code + PKCE)"
token_status="$(
  curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -o "${token_response}" -w '%{http_code}' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data "grant_type=authorization_code&client_id=$(urlencode "${CLIENT_ID}")&redirect_uri=$(urlencode "${REDIRECT_URI}")&code=$(urlencode "${authorization_code}")&code_verifier=$(urlencode "${code_verifier}")" \
    "${gateway_base_url}/oauth2/token"
)"
if [[ "${token_status}" != "200" ]]; then
  echo "Token exchange failed with status ${token_status}" >&2
  cat "${token_response}" >&2
  exit 1
fi

access_token="$(jq -r '.access_token // empty' "${token_response}")"
refresh_token="$(jq -r '.refresh_token // empty' "${token_response}")"
if [[ -z "${access_token}" ]]; then
  echo "Missing access_token in token response" >&2
  exit 1
fi
if [[ -z "${refresh_token}" ]]; then
  echo "Missing refresh_token in token response (offline_access not active)" >&2
  exit 1
fi

log "Step 5/6: /api/me before restart"
me_before="$(curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -H "Authorization: Bearer ${access_token}" "${gateway_base_url}/api/me")"
if [[ "$(jq -r '.authenticated // false' <<< "${me_before}")" != "true" ]]; then
  echo "Expected /api/me authenticated=true before restart" >&2
  echo "${me_before}" >&2
  exit 1
fi
log "/api/me before restart: ${me_before}"

log "Step 6/6: restart one auth pod and validate /api/me on restarted pod"
restarted_pod="$(restart_auth_pod)"
log "Port-forwarding restarted pod ${restarted_pod}:${AUTH_CONTAINER_PORT} -> localhost:${LOCAL_AUTH_PORT}"
kubectl -n "${NAMESPACE}" port-forward "pod/${restarted_pod}" "${LOCAL_AUTH_PORT}:${AUTH_CONTAINER_PORT}" >"${auth_pf_log}" 2>&1 &
auth_pf_pid=$!
wait_http_ok "http://127.0.0.1:${LOCAL_AUTH_PORT}/actuator/health" "${WAIT_TIMEOUT_SECONDS}"

for attempt in 1 2 3; do
  me_after="$(curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -H "Authorization: Bearer ${access_token}" "http://127.0.0.1:${LOCAL_AUTH_PORT}/api/me")"
  if [[ "$(jq -r '.authenticated // false' <<< "${me_after}")" != "true" ]]; then
    echo "Expected /api/me authenticated=true on restarted pod (attempt ${attempt})" >&2
    echo "${me_after}" >&2
    exit 1
  fi
done
log "/api/me after restart: ${me_after}"

log "OIDC smoke passed"
