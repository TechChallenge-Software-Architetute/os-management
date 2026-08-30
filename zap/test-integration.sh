#!/bin/sh
# Script de diagnóstico para validar pré-requisitos da varredura ZAP
# Uso: sh zap/test-integration.sh

APP_URL="${APP_URL:-http://localhost:8080}"
ZAP_AUTH_EMAIL="${ZAP_AUTH_EMAIL:-superadmin@system.com}"
ZAP_AUTH_PASSWORD="${ZAP_AUTH_PASSWORD:-coxinha123}"

PASS=0
FAIL=0

check() {
    LABEL="$1"
    RESULT="$2"
    if [ "$RESULT" = "ok" ]; then
        echo "  [PASS] $LABEL"
        PASS=$((PASS + 1))
    else
        echo "  [FAIL] $LABEL — $RESULT"
        FAIL=$((FAIL + 1))
    fi
}

echo "=============================================="
echo " Diagnóstico ZAP - os-management"
echo " App URL: $APP_URL"
echo "=============================================="

# --- Etapa 1: Conectividade básica ---
echo ""
echo "[1/5] Conectividade básica..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$APP_URL" 2>/dev/null)
if echo "$HTTP_CODE" | grep -qE "^[2345]"; then
    check "GET $APP_URL retorna HTTP $HTTP_CODE" "ok"
else
    check "GET $APP_URL" "sem resposta (HTTP: $HTTP_CODE)"
fi

# --- Etapa 2: Spec OpenAPI disponível ---
echo ""
echo "[2/5] Spec OpenAPI..."
SPEC_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$APP_URL/v3/api-docs" 2>/dev/null)
if [ "$SPEC_CODE" = "200" ]; then
    check "GET /v3/api-docs retorna 200" "ok"
else
    check "GET /v3/api-docs" "HTTP $SPEC_CODE (esperado 200)"
fi

# --- Etapa 3: Autenticação JWT ---
echo ""
echo "[3/5] Autenticação JWT..."
AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$APP_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"login\":\"$ZAP_AUTH_EMAIL\",\"password\":\"$ZAP_AUTH_PASSWORD\"}" 2>/dev/null)

AUTH_CODE=$(echo "$AUTH_RESPONSE" | tail -n 1)
LOGIN_BODY=$(echo "$AUTH_RESPONSE" | sed '$d')

if [ "$AUTH_CODE" = "200" ]; then
    check "POST /auth/login retorna 200" "ok"
    TOKEN=$(printf '%s' "$LOGIN_BODY" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
    if [ -n "$TOKEN" ]; then
        TOKEN_LEN=$(printf '%s' "$TOKEN" | wc -c)
        check "Token JWT extraído ($TOKEN_LEN chars)" "ok"
    else
        check "Token JWT extraído" "campo 'token' não encontrado na resposta"
        TOKEN=""
    fi
else
    check "POST /auth/login" "HTTP $AUTH_CODE (esperado 200)"
    TOKEN=""
fi

# --- Etapa 4: Endpoint protegido COM token ---
echo ""
echo "[4/5] Endpoint protegido (com token)..."
if [ -n "$TOKEN" ]; then
    PROT_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: Bearer $TOKEN" \
        "$APP_URL/clients" 2>/dev/null)
    if echo "$PROT_CODE" | grep -qE "^2"; then
        check "GET /clients com token retorna 2xx (HTTP $PROT_CODE)" "ok"
    else
        check "GET /clients com token" "HTTP $PROT_CODE (esperado 2xx)"
    fi
else
    check "GET /clients com token" "pulado (sem token)"
fi

# --- Etapa 5: Endpoint protegido SEM token ---
echo ""
echo "[5/5] Endpoint protegido (sem token)..."
NOAUTH_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$APP_URL/clients" 2>/dev/null)
if [ "$NOAUTH_CODE" = "401" ] || [ "$NOAUTH_CODE" = "403" ]; then
    check "GET /clients sem token retorna 401/403 (HTTP $NOAUTH_CODE)" "ok"
else
    check "GET /clients sem token" "HTTP $NOAUTH_CODE (esperado 401 ou 403)"
fi

# --- Resumo ---
echo ""
echo "=============================================="
echo " Resultado: $PASS passou(ram), $FAIL falhou(aram)"
echo "=============================================="
if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
exit 0
