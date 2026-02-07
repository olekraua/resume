#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-resume}"
GATEWAY_SERVICE="${GATEWAY_SERVICE:-resume-gateway}"
GATEWAY_PORT="${GATEWAY_PORT:-8080}"
LOCAL_GATEWAY_PORT="${LOCAL_GATEWAY_PORT:-18080}"
AUTH_SELECTOR="${AUTH_SELECTOR:-app=resume-auth-service}"
AUTH_DEPLOYMENT_NAME="${AUTH_DEPLOYMENT_NAME:-resume-auth-service}"
AUTH_CONTAINER_PORT="${AUTH_CONTAINER_PORT:-8081}"
LOCAL_AUTH_PORT="${LOCAL_AUTH_PORT:-18081}"
CLIENT_ID="${CLIENT_ID:-resume-spa}"
REDIRECT_URI="${REDIRECT_URI:-http://localhost:4200/auth/callback}"
OIDC_SCOPE="${OIDC_SCOPE:-openid profile offline_access}"
EXPECTED_ISSUER="${EXPECTED_ISSUER:-}"
SMOKE_USERNAME="${SMOKE_USERNAME:-}"
SMOKE_PASSWORD="${SMOKE_PASSWORD:-}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-300}"
HTTP_TIMEOUT_SECONDS="${HTTP_TIMEOUT_SECONDS:-15}"

if [[ -z "${SMOKE_USERNAME}" || -z "${SMOKE_PASSWORD}" ]]; then
  echo "SMOKE_USERNAME and SMOKE_PASSWORD are required" >&2
  exit 1
fi

for cmd in kubectl curl jq openssl awk sed grep seq; do
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
discovery_response="${tmp_dir}/discovery.json"
jwks_response="${tmp_dir}/jwks.json"
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

normalize_uri() {
  local uri="${1%/}"
  printf '%s\n' "${uri}"
}

uri_authority() {
  sed -E 's#^[a-zA-Z][a-zA-Z0-9+.-]*://##; s#/.*$##' <<< "$1"
}

uri_path() {
  local path
  path="$(sed -E 's#^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]+##' <<< "$1")"
  if [[ -z "${path}" ]]; then
    path="/"
  fi
  printf '%s\n' "${path}"
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

rollout_restart_auth_deployment() {
  log "Rolling restart for deployment/${AUTH_DEPLOYMENT_NAME}"
  kubectl -n "${NAMESPACE}" rollout restart "deployment/${AUTH_DEPLOYMENT_NAME}" >/dev/null
  kubectl -n "${NAMESPACE}" rollout status "deployment/${AUTH_DEPLOYMENT_NAME}" --timeout="${WAIT_TIMEOUT_SECONDS}s" >/dev/null
}

assert_api_me_authenticated() {
  local url="$1"
  local token="$2"
  local max_attempts="${3:-3}"
  local payload=""
  local authenticated="false"
  for attempt in $(seq 1 "${max_attempts}"); do
    payload="$(curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -H "Authorization: Bearer ${token}" "${url}" || true)"
    authenticated="$(jq -r '.authenticated // false' <<< "${payload}" 2>/dev/null || echo false)"
    if [[ "${authenticated}" == "true" ]]; then
      printf '%s\n' "${payload}"
      return 0
    fi
    sleep 2
  done
  echo "Expected /api/me authenticated=true at ${url}" >&2
  echo "${payload}" >&2
  return 1
}

log "Port-forwarding gateway service ${GATEWAY_SERVICE}:${GATEWAY_PORT} -> localhost:${LOCAL_GATEWAY_PORT}"
kubectl -n "${NAMESPACE}" port-forward "svc/${GATEWAY_SERVICE}" "${LOCAL_GATEWAY_PORT}:${GATEWAY_PORT}" >"${gateway_pf_log}" 2>&1 &
gateway_pf_pid=$!
wait_http_ok "http://127.0.0.1:${LOCAL_GATEWAY_PORT}/health" "${WAIT_TIMEOUT_SECONDS}"
gateway_base_url="http://127.0.0.1:${LOCAL_GATEWAY_PORT}"

if [[ -z "${EXPECTED_ISSUER}" ]]; then
  EXPECTED_ISSUER="$(kubectl -n "${NAMESPACE}" get configmap resume-common-config -o jsonpath='{.data.AUTH_ISSUER_URI}')"
fi
if [[ -z "${EXPECTED_ISSUER}" ]]; then
  echo "EXPECTED_ISSUER is empty and AUTH_ISSUER_URI is missing in resume-common-config" >&2
  exit 1
fi
EXPECTED_ISSUER="$(normalize_uri "${EXPECTED_ISSUER}")"

log "Step 1/8: OIDC discovery contract"
discovery_status="$(
  curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -o "${discovery_response}" -w '%{http_code}' \
    "${gateway_base_url}/.well-known/openid-configuration"
)"
if [[ "${discovery_status}" != "200" ]]; then
  echo "OIDC discovery failed with status ${discovery_status}" >&2
  cat "${discovery_response}" >&2
  exit 1
fi
discovery_issuer="$(jq -r '.issuer // empty' "${discovery_response}")"
discovery_jwks_uri="$(jq -r '.jwks_uri // empty' "${discovery_response}")"
if [[ -z "${discovery_issuer}" || -z "${discovery_jwks_uri}" ]]; then
  echo "OIDC discovery missing issuer or jwks_uri" >&2
  cat "${discovery_response}" >&2
  exit 1
fi
discovery_issuer="$(normalize_uri "${discovery_issuer}")"
if [[ "${discovery_issuer}" != "${EXPECTED_ISSUER}" ]]; then
  echo "OIDC discovery issuer mismatch: expected ${EXPECTED_ISSUER}, got ${discovery_issuer}" >&2
  exit 1
fi
expected_jwks_uri="${EXPECTED_ISSUER}/oauth2/jwks"
if [[ "${discovery_jwks_uri}" != "${expected_jwks_uri}" ]]; then
  echo "OIDC jwks_uri mismatch: expected ${expected_jwks_uri}, got ${discovery_jwks_uri}" >&2
  exit 1
fi
jwks_status="$(
  curl -sS --max-time "${HTTP_TIMEOUT_SECONDS}" -o "${jwks_response}" -w '%{http_code}' \
    -H "Host: $(uri_authority "${EXPECTED_ISSUER}")" \
    "${gateway_base_url}$(uri_path "${discovery_jwks_uri}")"
)"
if [[ "${jwks_status}" != "200" ]]; then
  echo "JWKS endpoint failed with status ${jwks_status}" >&2
  cat "${jwks_response}" >&2
  exit 1
fi
if ! jq -e '.keys | type == "array" and length > 0' "${jwks_response}" >/dev/null; then
  echo "JWKS response does not contain signing keys" >&2
  cat "${jwks_response}" >&2
  exit 1
fi
log "Discovery verified: issuer=${discovery_issuer}, jwks_uri=${discovery_jwks_uri}"

code_verifier="$(openssl rand -base64 72 | tr '+/' '-_' | tr -d '=[:space:]' | cut -c1-96)"
code_challenge="$(printf '%s' "${code_verifier}" | openssl dgst -binary -sha256 | openssl base64 -A | tr '+/' '-_' | tr -d '=')"
state="$(openssl rand -hex 16)"
nonce="$(openssl rand -hex 16)"

authorize_url="${gateway_base_url}/oauth2/authorize?response_type=code&client_id=$(urlencode "${CLIENT_ID}")&redirect_uri=$(urlencode "${REDIRECT_URI}")&scope=$(urlencode "${OIDC_SCOPE}")&code_challenge=$(urlencode "${code_challenge}")&code_challenge_method=S256&state=$(urlencode "${state}")&nonce=$(urlencode "${nonce}")"

log "Step 2/8: signinRedirect (authorize)"
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

log "Step 3/8: login form submit"
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

log "Step 4/8: callback redirect with authorization code"
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

log "Step 5/8: token exchange (authorization_code + PKCE)"
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

log "Step 6/8: /api/me before restart"
me_before="$(assert_api_me_authenticated "${gateway_base_url}/api/me" "${access_token}" 3)"
log "/api/me before restart: ${me_before}"

log "Step 7/8: restart one auth pod and validate /api/me on restarted pod"
restarted_pod="$(restart_auth_pod)"
log "Port-forwarding restarted pod ${restarted_pod}:${AUTH_CONTAINER_PORT} -> localhost:${LOCAL_AUTH_PORT}"
kubectl -n "${NAMESPACE}" port-forward "pod/${restarted_pod}" "${LOCAL_AUTH_PORT}:${AUTH_CONTAINER_PORT}" >"${auth_pf_log}" 2>&1 &
auth_pf_pid=$!
wait_http_ok "http://127.0.0.1:${LOCAL_AUTH_PORT}/actuator/health" "${WAIT_TIMEOUT_SECONDS}"

me_after="$(assert_api_me_authenticated "http://127.0.0.1:${LOCAL_AUTH_PORT}/api/me" "${access_token}" 3)"
log "/api/me after restart: ${me_after}"

log "Step 8/8: rollout restart auth deployment and validate /api/me via gateway"
rollout_restart_auth_deployment
me_after_rollout="$(assert_api_me_authenticated "${gateway_base_url}/api/me" "${access_token}" 5)"
log "/api/me after rollout: ${me_after_rollout}"

log "OIDC smoke passed"
