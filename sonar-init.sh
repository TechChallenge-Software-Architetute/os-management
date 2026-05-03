#!/bin/sh
set -eu

SONAR_HOST_URL="${SONAR_HOST_URL:-http://sonarqube:9000}"
SONAR_ADMIN_USER="${SONAR_ADMIN_USER:-admin}"
SONAR_ADMIN_DEFAULT_PASSWORD="${SONAR_ADMIN_DEFAULT_PASSWORD:-admin}"
SONAR_ADMIN_PASSWORD="${SONAR_ADMIN_PASSWORD:-admin123}"
SONAR_PROJECT_KEY="${SONAR_PROJECT_KEY:-os-management}"
SONAR_PROJECT_NAME="${SONAR_PROJECT_NAME:-os-management}"
SONAR_TOKEN_NAME="${SONAR_TOKEN_NAME:-os-management-token}"
SONAR_TOKEN_FILE="${SONAR_TOKEN_FILE:-/sonar/token}"
SONAR_TOKEN="${SONAR_TOKEN:-}"

echo "Aguardando SonarQube ficar pronto em ${SONAR_HOST_URL}..."
until curl --silent --fail "${SONAR_HOST_URL}/api/system/status" | grep -Eq '"status":"(UP|DEGRADED)"'; do
  sleep 5
done

if [ -n "${SONAR_TOKEN}" ]; then
  echo "Usando token informado por variavel de ambiente."
  mkdir -p "$(dirname "${SONAR_TOKEN_FILE}")"
  printf '%s' "${SONAR_TOKEN}" >"${SONAR_TOKEN_FILE}"

  echo "Garantindo projeto ${SONAR_PROJECT_KEY} no SonarQube com token informado..."
  curl --silent --show-error --request POST \
    --header "Authorization: Bearer ${SONAR_TOKEN}" \
    --url "${SONAR_HOST_URL}/api/projects/create" \
    --data-urlencode "project=${SONAR_PROJECT_KEY}" \
    --data-urlencode "name=${SONAR_PROJECT_NAME}" >/tmp/sonar-project-create.json || true

  echo "Bootstrap do SonarQube concluido."
  exit 0
fi

if curl --silent --fail --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASSWORD}" \
  "${SONAR_HOST_URL}/api/authentication/validate" | grep -q '"valid":true'; then
  echo "Login admin do SonarQube ja esta configurado."
elif curl --silent --fail --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_DEFAULT_PASSWORD}" \
  "${SONAR_HOST_URL}/api/authentication/validate" | grep -q '"valid":true'; then
  echo "Alterando senha default do admin do SonarQube..."
  curl --silent --show-error --fail --request POST \
    --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_DEFAULT_PASSWORD}" \
    --url "${SONAR_HOST_URL}/api/users/change_password" \
    --data-urlencode "login=${SONAR_ADMIN_USER}" \
    --data-urlencode "previousPassword=${SONAR_ADMIN_DEFAULT_PASSWORD}" \
    --data-urlencode "password=${SONAR_ADMIN_PASSWORD}" >/dev/null
else
  echo "Nao foi possivel autenticar no SonarQube com a senha configurada nem com a senha default."
  echo "Se este volume do SonarQube ja foi configurado manualmente, informe SONAR_ADMIN_PASSWORD com a senha atual ou SONAR_TOKEN com um token valido."
  exit 1
fi

echo "Garantindo projeto ${SONAR_PROJECT_KEY} no SonarQube..."
curl --silent --show-error --request POST \
  --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASSWORD}" \
  --url "${SONAR_HOST_URL}/api/projects/create" \
  --data-urlencode "project=${SONAR_PROJECT_KEY}" \
  --data-urlencode "name=${SONAR_PROJECT_NAME}" >/tmp/sonar-project-create.json || true

echo "Gerando token ${SONAR_TOKEN_NAME}..."
curl --silent --show-error --request POST \
  --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASSWORD}" \
  --url "${SONAR_HOST_URL}/api/user_tokens/revoke" \
  --data-urlencode "name=${SONAR_TOKEN_NAME}" >/dev/null || true

TOKEN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --user "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASSWORD}" \
  --url "${SONAR_HOST_URL}/api/user_tokens/generate" \
  --data-urlencode "name=${SONAR_TOKEN_NAME}")"

TOKEN="$(printf '%s' "${TOKEN_RESPONSE}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

if [ -z "${TOKEN}" ]; then
  echo "Nao foi possivel obter o token do SonarQube."
  echo "${TOKEN_RESPONSE}"
  exit 1
fi

mkdir -p "$(dirname "${SONAR_TOKEN_FILE}")"
printf '%s' "${TOKEN}" >"${SONAR_TOKEN_FILE}"

echo "Bootstrap do SonarQube concluido."
