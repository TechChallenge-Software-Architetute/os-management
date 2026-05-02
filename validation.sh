#!/bin/sh
set -e

echo "=========================================="
echo "Iniciando validacao de fluxo "
echo "=========================================="

APP_URL="${APP_URL:-http://localhost:8080}"

i=0
until curl -s -o /dev/null "$APP_URL"; do
  i=$((i + 1))
  if [ "$i" -ge 30 ]; then
    echo "API nao respondeu em $APP_URL"
    exit 1
  fi

  echo "Aguardando API..."
  sleep 1
done

echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 1. Autenticando usuario superadmin."
echo "------------------------------------------------------------------------------"
LOGIN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/auth/login" \
  --header 'content-type: application/json' \
  --data '{
  "email": "superadmin@system.com",
  "password": "coxinha123"
}')"

TOKEN="$(printf '%s' "$LOGIN_RESPONSE" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

if [ -z "$TOKEN" ]; then
  echo "Nao foi possivel obter token de autenticacao."
  echo "$LOGIN_RESPONSE"
  exit 1
fi

echo "------------------------------------------------------------------------------"
echo "Token obtido com sucesso."
echo "------------------------------------------------------------------------------"

echo " "
echo " "
echo "-------------------------------------------------------------------"
echo " - 2. Criando ordem de servico."
echo "------------------------------------------------------------------------------"
ORDER_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/order" \
  --header "authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --header 'correlationid: d29797dd-0eca-4ee0-918d-466ed0c8886e' \
  --data '{
  "cpfCnpj": "529.982.247-25",
  "placaVeiculo": "ABC-1234",
  "serviceTypes": [
    "TROCA_OLEO",
    "ALINHAMENTO"
  ]
}')"

ORDER_ID="$(printf '%s' "$ORDER_RESPONSE" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
export ORDER_ID

if [ -z "$ORDER_ID" ]; then
  echo "Nao foi possivel obter o id da ordem de servico."
  echo "$ORDER_RESPONSE"
  exit 1
fi

echo "ORDER_ID=$ORDER_ID"

sleep 2

echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 3. Mecanico comecou a realizar diagnostico [EM_DIAGNOSTICO]."
echo "------------------------------------------------------------------------------"
curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "EM_DIAGNOSTICO"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 4. Mecanico Consulta Ordem de Serviço."
echo "------------------------------------------------------------------------------"

curl --request GET \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 5. Mecanico Consulta Peças para Ordem de Serviço."
echo "------------------------------------------------------------------------------"

curl --request GET \
  --url "$APP_URL/api/parts" \
  --header "authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 6. Mecanico Consulta Estoque para Ordem de Serviço."
echo "------------------------------------------------------------------------------"

curl --request GET \
  --url "$APP_URL/api/stocks" \
  --header "authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 7. Mecanico Reserva Estoque para Ordem de Serviço."
echo "------------------------------------------------------------------------------"

curl --request POST \
  --url "$APP_URL/api/stocks/reservations" \
  --header "authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "serviceOrderId": "$ORDER_ID",
  "items": [
    {
      "productId": 1,
      "quantity": 3.5
    }
  ]
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 8. Mecanico Termina avaliação Ordem de Serviço [AGUARDANDO_APROVACAO]."
echo "------------------------------------------------------------------------------"

curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "AGUARDANDO_APROVACAO"
}'


echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 9. Cliente Aprova Orçamento ."
echo "------------------------------------------------------------------------------"

curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "APROVADO"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 10. Mecanico visualiza serviços a serem feitos ."
echo "------------------------------------------------------------------------------"

curl --request GET \
  --url "$APP_URL/services/os/$ORDER_ID" \
  --header "authorization: Bearer $TOKEN"