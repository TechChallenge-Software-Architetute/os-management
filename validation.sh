#!/bin/sh
set -e

echo "=========================================="
echo "Iniciando validacao de fluxo"
echo "=========================================="

APP_URL="${APP_URL:-http://localhost:8080}"

run_curl() {
  curl --silent --show-error --fail "$@"
  echo
}

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
echo "------------------------------------------------------------------------------"
echo " - 2. Criando ordem de servico."
echo "------------------------------------------------------------------------------"
ORDER_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/order" \
  --header "Authorization: Bearer $TOKEN" \
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
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "EM_DIAGNOSTICO"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 4. Mecanico consulta ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 5. Mecanico consulta pecas para ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/parts" \
  --header "Authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 6. Mecanico consulta estoque para ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/stocks" \
  --header "Authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 7. Mecanico reserva estoque para ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request POST \
  --url "$APP_URL/api/stocks/reservations" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"serviceOrderId\": \"$ORDER_ID\",
  \"items\": [
    {
      \"productId\": 1,
      \"quantity\": 3.5
    }
  ]
}"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 8. Mecanico termina avaliacao da ordem de servico [AGUARDANDO_APROVACAO]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "AGUARDANDO_APROVACAO"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 9. Mecanico consulta orcamento da ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/budgets/service-order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 10. Cliente aprova Ordem de Serviço [APROVADO]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "APROVADO"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 11. Mecanico visualiza servicos a serem feitos."
echo "------------------------------------------------------------------------------"
SERVICES_RESPONSE="$(curl --silent --show-error --fail --request GET \
  --url "$APP_URL/services/os/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN")"

echo "$SERVICES_RESPONSE"

SERVICE_IDS="$(printf '%s' "$SERVICES_RESPONSE" \
  | sed 's/[{}]/\
/g' \
  | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

service1="$(printf '%s\n' "$SERVICE_IDS" | sed -n '1p')"
service2="$(printf '%s\n' "$SERVICE_IDS" | sed -n '2p')"
export service1
export service2

if [ -z "$service1" ] || [ -z "$service2" ]; then
  echo "Nao foi possivel obter os ids dos dois servicos."
  echo "$SERVICES_RESPONSE"
  exit 1
fi

echo "service1=$service1"
echo "service2=$service2"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 12. Mecanico inicia execucao do primeiro servico [DOING]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DOING\",
  \"id\": \"$service1\"
}"

sleep 5
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 13. Mecanico finaliza execucao do primeiro servico [DONE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service1\"
}"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 14. Mecanico inicia execucao do segundo servico [DOING]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DOING\",
  \"id\": \"$service2\"
}"

sleep 7
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 15. Mecanico finaliza execucao do segundo servico [DONE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service2\"
}"

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 16. Finalizando ordem de servico [FINALIZADA]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "FINALIZADA"
}'

sleep 2
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 17. Entregando veiculo ao cliente [ENTREGUE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "ENTREGUE"
}'

sleep 4
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 18. Monitoracao de tempo de execucao por servico."
echo "------------------------------------------------------------------------------"
curl --request POST \
  --url "$APP_URL/monitoring/all" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "timeUnit": "SECONDS"
}'
