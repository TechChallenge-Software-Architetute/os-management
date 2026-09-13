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

hdr=(-H "DD-API-KEY: ${DD_API_KEY}" -H "DD-APPLICATION-KEY: ${DD_APP_KEY}" -H "Content-Type: application/json")

# --- Dashboard (match by title) ---
dash_file="datadog/dashboards/os-management.json"
title="$(jq -r '.title' "${dash_file}")"
dash_id="$(curl -sf "${hdr[@]}" "${API}/api/v1/dashboard" \
  | jq -r --arg t "${title}" '.dashboards[]? | select(.title==$t) | .id' | head -n1)"

if [ -n "${dash_id}" ] && [ "${dash_id}" != "null" ]; then
  echo "Atualizando dashboard '${title}' (${dash_id})"
  curl -sf -X PUT "${hdr[@]}" "${API}/api/v1/dashboard/${dash_id}" -d @"${dash_file}" >/dev/null
else
  echo "Criando dashboard '${title}'"
  curl -sf -X POST "${hdr[@]}" "${API}/api/v1/dashboard" -d @"${dash_file}" >/dev/null
fi

# --- Monitors (match by name) ---
all_monitors="$(curl -sf "${hdr[@]}" "${API}/api/v1/monitor")"
for f in datadog/monitors/*.json; do
  name="$(jq -r '.name' "${f}")"
  mid="$(echo "${all_monitors}" | jq -r --arg n "${name}" '.[]? | select(.name==$n) | .id' | head -n1)"
  if [ -n "${mid}" ] && [ "${mid}" != "null" ]; then
    echo "Atualizando monitor '${name}' (${mid})"
    curl -sf -X PUT "${hdr[@]}" "${API}/api/v1/monitor/${mid}" -d @"${f}" >/dev/null
  else
    echo "Criando monitor '${name}'"
    curl -sf -X POST "${hdr[@]}" "${API}/api/v1/monitor" -d @"${f}" >/dev/null
  fi
done

echo "Datadog: dashboard e monitores aplicados."
