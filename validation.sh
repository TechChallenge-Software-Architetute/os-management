#!/bin/sh
set -e

echo "=========================================="
echo "Iniciando validacao de fluxo"
echo "=========================================="

APP_URL="${APP_URL:-http://localhost:8080}"
TIMEOUT_CONST=2

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
  "cpfCnpj": "52998224725",
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

sleep $TIMEOUT_CONST
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

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 4. Mecanico consulta ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 5. Mecanico consulta pecas para ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/parts" \
  --header "Authorization: Bearer $TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 6. Mecanico consulta estoque para ordem de servico."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/stocks" \
  --header "Authorization: Bearer $TOKEN"

sleep $TIMEOUT_CONST
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

sleep $TIMEOUT_CONST
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

# sleep $TIMEOUT_CONST
# echo " "
# echo " "
# echo "------------------------------------------------------------------------------"
# echo " - 9. Mecanico consulta orcamento da ordem de servico."
# echo "------------------------------------------------------------------------------"
# run_curl --request GET \
#   --url "$APP_URL/api/budgets/service-order/$ORDER_ID" \
#   --header "Authorization: Bearer $TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 10. Criando acesso do cliente Joao."
echo "------------------------------------------------------------------------------"
SIGNUP_RESPONSE_FILE="/tmp/os-management-signup-response.json"
SIGNUP_STATUS="$(curl --silent --show-error --request POST \
  --url "$APP_URL/signup" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --output "$SIGNUP_RESPONSE_FILE" \
  --write-out '%{http_code}' \
  --data '{
  "email": "joao.silva@email.com",
  "password": "Coxinha321",
  "roles": [
    "USER"
  ]
}')"

cat "$SIGNUP_RESPONSE_FILE"
echo

case "$SIGNUP_STATUS" in
  2*) ;;
  *)
    echo "Cadastro do cliente Joao nao retornou sucesso (HTTP $SIGNUP_STATUS). Tentando login com usuario existente."
    ;;
esac

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 11. Criando token para sessao do cliente Joao."
echo "------------------------------------------------------------------------------"
JOAO_LOGIN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/auth/login" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "email": "joao.silva@email.com",
  "password": "Coxinha321"
}')"

JOAO_TOKEN="$(printf '%s' "$JOAO_LOGIN_RESPONSE" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

if [ -z "$JOAO_TOKEN" ]; then
  echo "Nao foi possivel obter token de autenticacao do cliente Joao."
  echo "$JOAO_LOGIN_RESPONSE"
  exit 1
fi

echo "Token do cliente Joao obtido com sucesso."

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 12. Cliente Joao lista todos seus orcamentos."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/clients/my-orders" \
  --header "Authorization: Bearer $JOAO_TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 13. Cliente Joao visualiza detalhes do orcamento desejado."
echo "------------------------------------------------------------------------------"
run_curl --request GET \
  --url "$APP_URL/api/clients/my-orders/$ORDER_ID" \
  --header "Authorization: Bearer $JOAO_TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 14. Cliente Joao aprova o orcamento [APROVADO]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/api/clients/my-orders/$ORDER_ID/approve" \
  --header "Authorization: Bearer $JOAO_TOKEN"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 15. Mecanico visualiza servicos a serem feitos."
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

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 16. Mecanico inicia execucao do primeiro servico [DOING]."
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
echo " - 17. Mecanico finaliza execucao do primeiro servico [DONE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service1\"
}"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 18. Mecanico inicia execucao do segundo servico [DOING]."
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
echo " - 19. Mecanico finaliza execucao do segundo servico [DONE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service2\"
}"

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 20. Finalizando ordem de servico [FINALIZADA]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "FINALIZADA"
}'

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 21. Entregando veiculo ao cliente [ENTREGUE]."
echo "------------------------------------------------------------------------------"
run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "ENTREGUE"
}'

sleep $TIMEOUT_CONST
echo " "
echo " "
echo "------------------------------------------------------------------------------"
echo " - 22. Monitoracao de tempo de execucao por servico."
echo "------------------------------------------------------------------------------"
run_curl --request POST \
  --url "$APP_URL/monitoring/all" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "timeUnit": "SECONDS"
}'
