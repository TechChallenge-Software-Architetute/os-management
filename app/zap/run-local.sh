#!/bin/sh
# Executa a varredura ZAP localmente (fora do Docker Compose)
# Requer: Docker em execução e aplicação rodando em localhost:8080
#
# Uso:
#   sh zap/run-local.sh
#
# Variáveis de ambiente opcionais:
#   APP_URL            (padrão: http://host.docker.internal:8080)
#   ZAP_AUTH_EMAIL     (padrão: superadmin@system.com)
#   ZAP_AUTH_PASSWORD  (padrão: coxinha123)

APP_URL="${APP_URL:-http://host.docker.internal:8080}"
ZAP_AUTH_EMAIL="${ZAP_AUTH_EMAIL:-superadmin@system.com}"
ZAP_AUTH_PASSWORD="${ZAP_AUTH_PASSWORD:-coxinha123}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
REPORTS_DIR="$PROJECT_DIR/zap-reports"

mkdir -p "$REPORTS_DIR"

echo "Iniciando ZAP scan local..."
echo "  App URL    : $APP_URL"
echo "  Reports dir: $REPORTS_DIR"
echo ""

docker run --rm \
    -v "$SCRIPT_DIR:/zap/wrk:ro" \
    -v "$REPORTS_DIR:/zap/reports:rw" \
    -e APP_URL="$APP_URL" \
    -e ZAP_AUTH_EMAIL="$ZAP_AUTH_EMAIL" \
    -e ZAP_AUTH_PASSWORD="$ZAP_AUTH_PASSWORD" \
    -e REPORT_DIR="/zap/reports" \
    ghcr.io/zaproxy/zaproxy:stable \
    sh /zap/wrk/zap-scan.sh
