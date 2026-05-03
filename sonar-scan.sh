#!/bin/sh
set -eu

SONAR_HOST_URL="${SONAR_HOST_URL:-http://sonarqube:9000}"
SONAR_PUBLIC_URL="${SONAR_PUBLIC_URL:-http://localhost:9000}"
SONAR_PROJECT_KEY="${SONAR_PROJECT_KEY:-os-management}"
SONAR_PROJECT_NAME="${SONAR_PROJECT_NAME:-os-management}"
SONAR_TOKEN_FILE="${SONAR_TOKEN_FILE:-/sonar/token}"
SONAR_METADATA_FILE="${SONAR_METADATA_FILE:-target/sonar/report-task.txt}"

if [ ! -s "${SONAR_TOKEN_FILE}" ]; then
  echo "Token do SonarQube nao encontrado em ${SONAR_TOKEN_FILE}."
  exit 1
fi

SONAR_TOKEN="$(cat "${SONAR_TOKEN_FILE}")"

mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
  -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
  -Dsonar.host.url="${SONAR_HOST_URL}" \
  -Dsonar.token="${SONAR_TOKEN}" \
  -Dsonar.scanner.metadataFilePath="${SONAR_METADATA_FILE}"

echo ""
echo "============================================================"
echo "SonarQube analysis submitted successfully."
echo "Project dashboard:"
echo "${SONAR_PUBLIC_URL}/dashboard?id=${SONAR_PROJECT_KEY}"

if [ -f "${SONAR_METADATA_FILE}" ]; then
  CE_TASK_URL="$(sed -n 's/^ceTaskUrl=//p' "${SONAR_METADATA_FILE}")"

  if [ -n "${CE_TASK_URL}" ]; then
    echo "Background task:"
    echo "${CE_TASK_URL}"
  fi
fi

echo "============================================================"
