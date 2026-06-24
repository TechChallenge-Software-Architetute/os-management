#!/bin/sh
# OWASP ZAP API Scan - os-management
# Autentica via JWT e executa varredura DAST na API

APP_URL="${APP_URL:-http://app:8080}"
ZAP_AUTH_EMAIL="${ZAP_AUTH_EMAIL:-superadmin@system.com}"
ZAP_AUTH_PASSWORD="${ZAP_AUTH_PASSWORD:-coxinha123}"
REPORT_DIR="${REPORT_DIR:-/zap/reports}"
RULES_FILE="/zap/wrk/rules.conf"

echo "=============================================="
echo " OWASP ZAP - Varredura de Segurança"
echo "=============================================="
echo " App URL   : $APP_URL"
echo " Auth email: $ZAP_AUTH_EMAIL"
echo " Reports   : $REPORT_DIR"
echo "=============================================="

mkdir -p "$REPORT_DIR"

# -------------------------------------------------
# 1. Aguardar aplicação ficar disponível
# -------------------------------------------------
echo ""
echo "[1/4] Aguardando aplicação em $APP_URL ..."
i=0
while true; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$APP_URL" 2>/dev/null)
    if echo "$HTTP_CODE" | grep -qE "^[2345]"; then
        echo "  Aplicação disponível (HTTP $HTTP_CODE)."
        break
    fi
    i=$((i + 1))
    if [ "$i" -ge 40 ]; then
        echo "ERRO: Aplicação não respondeu após 80 segundos. Abortando."
        exit 1
    fi
    echo "  Tentativa $i/40 — aguardando 2s..."
    sleep 2
done

# -------------------------------------------------
# 2. Autenticar e obter token JWT
# -------------------------------------------------
echo ""
echo "[2/4] Autenticando em $APP_URL/auth/login ..."

AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$APP_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$ZAP_AUTH_EMAIL\",\"password\":\"$ZAP_AUTH_PASSWORD\"}" \
    2>/dev/null)

AUTH_CODE=$(echo "$AUTH_RESPONSE" | tail -n 1)
LOGIN_BODY=$(echo "$AUTH_RESPONSE" | sed '$d')

echo "  HTTP status: $AUTH_CODE"

if [ "$AUTH_CODE" != "200" ]; then
    echo "ERRO: POST /auth/login retornou HTTP $AUTH_CODE."
    echo "Resposta: $LOGIN_BODY"
    echo ""
    echo "Verifique as variáveis ZAP_AUTH_EMAIL e ZAP_AUTH_PASSWORD."
    exit 1
fi

TOKEN=$(printf '%s' "$LOGIN_BODY" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
    echo "ERRO: Token JWT não encontrado na resposta."
    echo "Resposta: $LOGIN_BODY"
    exit 1
fi

TOKEN_LEN=$(printf '%s' "$TOKEN" | wc -c)
echo "  Token JWT obtido com sucesso ($TOKEN_LEN caracteres)."

# -------------------------------------------------
# 3. Montar configuração do Replacer (injeção JWT)
# -------------------------------------------------
REPLACER_CONFIG="\
-config replacer.full_list(0).description=JWT_Auth \
-config replacer.full_list(0).enabled=true \
-config replacer.full_list(0).matchtype=REQ_HEADER \
-config replacer.full_list(0).matchstr=Authorization \
-config replacer.full_list(0).matchregex=false \
-config replacer.full_list(0).replacement=Bearer\ $TOKEN \
-config replacer.full_list(0).initiators="

# -------------------------------------------------
# 4. Executar varredura ZAP
# -------------------------------------------------
echo ""
echo "[3/4] Iniciando varredura OWASP ZAP..."
echo "  OpenAPI spec : $APP_URL/v3/api-docs"
echo "  Rules file   : $RULES_FILE"
echo "  HTML report  : $REPORT_DIR/zap-report.html"
echo "  JSON report  : $REPORT_DIR/zap-report.json"
echo ""

zap-api-scan.py \
    -t "$APP_URL/v3/api-docs" \
    -f openapi \
    -r "$REPORT_DIR/zap-report.html" \
    -J "$REPORT_DIR/zap-report.json" \
    -c "$RULES_FILE" \
    -z "$REPLACER_CONFIG" \
    -I \
    > "$REPORT_DIR/zap-scan.log" 2>&1

ZAP_EXIT=$?
cat "$REPORT_DIR/zap-scan.log"

# -------------------------------------------------
# 5. Resultado
# -------------------------------------------------
echo ""
echo "[4/4] Varredura concluída (exit code: $ZAP_EXIT)."
echo ""
if [ -f "$REPORT_DIR/zap-report.html" ]; then
    echo "  Relatório HTML : $REPORT_DIR/zap-report.html"
else
    echo "  AVISO: Relatório HTML não foi gerado."
fi
if [ -f "$REPORT_DIR/zap-report.json" ]; then
    echo "  Relatório JSON : $REPORT_DIR/zap-report.json"
fi

echo ""
echo "=============================================="
echo " ZAP finalizado. Verifique os relatórios."
echo "=============================================="

# Sempre sair com 0 para não bloquear o pipeline
exit 0
