#!/usr/bin/env bash
# Upsert the versioned Datadog dashboard and monitors via the API.
# Idempotent: matches by dashboard title / monitor name, so re-running on every
# push updates in place instead of creating duplicates.
#
# Requires: DD_API_KEY, DD_APP_KEY, DD_SITE (e.g. datadoghq.com). Needs jq + curl.
set -euo pipefail

: "${DD_API_KEY:?DD_API_KEY is required}"
: "${DD_APP_KEY:?DD_APP_KEY is required}"
DD_SITE="${DD_SITE:-datadoghq.com}"
API="https://api.${DD_SITE}"

# Authenticated API call. Args: METHOD URL [DATA_FILE].
# Prints the response body on success; on HTTP >= 400 prints the endpoint,
# status and body to stderr and returns 1 (so failures are actionable, not "exit 22").
dd_api() {
  local method="$1" url="$2" data_file="${3:-}"
  local curl_args=(-sS -X "${method}"
    -H "DD-API-KEY: ${DD_API_KEY}"
    -H "DD-APPLICATION-KEY: ${DD_APP_KEY}"
    -H "Content-Type: application/json"
    -w $'\n%{http_code}')
  [ -n "${data_file}" ] && curl_args+=(--data-binary @"${data_file}")

  local out code body
  out="$(curl "${curl_args[@]}" "${url}")" || { echo "::error::curl transport error on ${method} ${url}" >&2; return 1; }
  code="${out##*$'\n'}"
  body="${out%$'\n'*}"
  if [ "${code}" -ge 400 ]; then
    echo "::error::Datadog API ${method} ${url} returned HTTP ${code}" >&2
    printf '%s\n' "${body}" | head -c 2000 >&2; echo >&2
    return 1
  fi
  printf '%s' "${body}"
}

echo "Datadog site: ${DD_SITE}"

# Preflight: validates the API+APP keys, their scopes and DD_SITE up front with a
# clear message instead of a cryptic failure mid-run.
if ! dashboards_json="$(dd_api GET "${API}/api/v1/dashboard")"; then
  echo "::error::Could not list dashboards. Check DD_API_KEY, DD_APP_KEY (needs dashboards_read/write + monitors_read/write) and DD_SITE (currently '${DD_SITE}' — must match your Datadog org's site)." >&2
  exit 1
fi

# --- Dashboard (match by title) ---
dash_file="datadog/dashboards/os-management.json"
title="$(jq -r '.title' "${dash_file}")"
dash_id="$(printf '%s' "${dashboards_json}" | jq -r --arg t "${title}" '.dashboards[]? | select(.title==$t) | .id' | head -n1)"

if [ -n "${dash_id}" ] && [ "${dash_id}" != "null" ]; then
  echo "Atualizando dashboard '${title}' (${dash_id})"
  dd_api PUT "${API}/api/v1/dashboard/${dash_id}" "${dash_file}" >/dev/null
else
  echo "Criando dashboard '${title}'"
  dd_api POST "${API}/api/v1/dashboard" "${dash_file}" >/dev/null
fi

# --- Monitors (match by name) ---
all_monitors="$(dd_api GET "${API}/api/v1/monitor")"
for f in datadog/monitors/*.json; do
  name="$(jq -r '.name' "${f}")"
  mid="$(printf '%s' "${all_monitors}" | jq -r --arg n "${name}" '.[]? | select(.name==$n) | .id' | head -n1)"
  if [ -n "${mid}" ] && [ "${mid}" != "null" ]; then
    echo "Atualizando monitor '${name}' (${mid})"
    dd_api PUT "${API}/api/v1/monitor/${mid}" "${f}" >/dev/null
  else
    echo "Criando monitor '${name}'"
    dd_api POST "${API}/api/v1/monitor" "${f}" >/dev/null
  fi
done

echo "Datadog: dashboard e monitores aplicados."
