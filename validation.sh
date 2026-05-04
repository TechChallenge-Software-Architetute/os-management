#!/bin/sh
set -e

echo "=========================================="
echo "Iniciando validacao de fluxo"
echo "=========================================="

APP_URL="${APP_URL:-http://localhost:8080}"
TIMEOUT_CONST=2

SUPERADMIN_EMAIL="superadmin@system.com"
SUPERADMIN_PASSWORD="coxinha123"

CLIENT_USER_EMAIL="joao.silva2@email.com"
CLIENT_USER_PASSWORD="coxinha123"

CLIENT_CPF="93541134771"
CLIENT_EMAIL="$CLIENT_USER_EMAIL"
VEHICLE_PLATE="VAL-1224"

PART_SKU="VAL-PART-011"
SUPPLY_SKU="VAL-SUP-011"

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

step "Autenticando usuario superadmin."

LOGIN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/auth/login" \
  --header 'content-type: application/json' \
  --data "{
  \"email\": \"$SUPERADMIN_EMAIL\",
  \"password\": \"$SUPERADMIN_PASSWORD\"
}")"

TOKEN="$(extract_json_string "$LOGIN_RESPONSE" token)"
require_value "token de autenticacao" "$TOKEN" "$LOGIN_RESPONSE"

echo "------------------------------------------------------------------------------"
echo "Token obtido com sucesso."
echo "------------------------------------------------------------------------------"

step "Criando usuario do portal do cliente."

run_curl --request POST \
  --url "$APP_URL/signup" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"email\": \"$CLIENT_USER_EMAIL\",
  \"password\": \"$CLIENT_USER_PASSWORD\",
  \"roles\": [\"ROLE_USER\"]
}"

step "Autenticando usuario do portal do cliente."

CLIENT_LOGIN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/auth/login" \
  --header 'content-type: application/json' \
  --data "{
  \"email\": \"$CLIENT_USER_EMAIL\",
  \"password\": \"$CLIENT_USER_PASSWORD\"
}")"

CLIENT_TOKEN="$(extract_json_string "$CLIENT_LOGIN_RESPONSE" token)"
require_value "token do cliente" "$CLIENT_TOKEN" "$CLIENT_LOGIN_RESPONSE"

echo "------------------------------------------------------------------------------"
echo "Token do cliente obtido com sucesso."
echo "------------------------------------------------------------------------------"

step "Criando cliente de validacao."

CLIENT_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/api/clients" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Cliente Validacao\",
  \"cpf\": \"$CLIENT_CPF\",
  \"email\": \"$CLIENT_EMAIL\",
  \"phone\": \"(11) 90000-0100\"
}")"

CLIENT_ID="$(extract_json_number "$CLIENT_RESPONSE" id)"
require_value "id do cliente de validacao" "$CLIENT_ID" "$CLIENT_RESPONSE"

echo "CLIENT_ID=$CLIENT_ID"

step "Listando clientes."

run_curl --request GET \
  --url "$APP_URL/api/clients" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando cliente por ID."

run_curl --request GET \
  --url "$APP_URL/api/clients/$CLIENT_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando cliente por CPF."

run_curl --request GET \
  --url "$APP_URL/api/clients/cpf/$CLIENT_CPF" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando cliente de validacao."

run_curl --request PUT \
  --url "$APP_URL/api/clients/$CLIENT_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Cliente Validacao Atualizado\",
  \"cpf\": \"$CLIENT_CPF\",
  \"email\": \"$CLIENT_EMAIL\",
  \"phone\": \"(11) 90000-0001\"
}"

step "Criando veiculo de validacao."

VEHICLE_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/api/vehicles" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"clientId\": $CLIENT_ID,
  \"plate\": \"$VEHICLE_PLATE\",
  \"brand\": \"Toyota\",
  \"model\": \"Corolla\",
  \"year\": 2020,
  \"color\": \"Prata\",
  \"type\": \"CAR\"
}")"

VEHICLE_ID="$(extract_json_number "$VEHICLE_RESPONSE" id)"
require_value "id do veiculo de validacao" "$VEHICLE_ID" "$VEHICLE_RESPONSE"

echo "VEHICLE_ID=$VEHICLE_ID"

step "Consultando veiculo por ID."

run_curl --request GET \
  --url "$APP_URL/api/vehicles/$VEHICLE_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando veiculo por placa."

run_curl --request GET \
  --url "$APP_URL/api/vehicles/plate/$VEHICLE_PLATE" \
  --header "Authorization: Bearer $TOKEN"

step "Listando veiculos por cliente."

run_curl --request GET \
  --url "$APP_URL/api/vehicles/client/$CLIENT_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando veiculo de validacao."

run_curl --request PUT \
  --url "$APP_URL/api/vehicles/$VEHICLE_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"clientId\": $CLIENT_ID,
  \"plate\": \"$VEHICLE_PLATE\",
  \"brand\": \"Toyota\",
  \"model\": \"Corolla XEI\",
  \"year\": 2021,
  \"color\": \"Preto\",
  \"type\": \"CAR\"
}"

step "Criando peca de validacao."

PART_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/api/parts" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Filtro de Ar Validacao\",
  \"sku\": \"$PART_SKU\",
  \"unit\": \"UNIT\",
  \"category\": \"Filtros\",
  \"brand\": \"Bosch\",
  \"costPrice\": 35.00,
  \"salePrice\": 70.00,
  \"manufacturerCode\": \"VAL-FLT-001\",
  \"warrantyMonths\": 6
}")"

PART_ID="$(extract_json_number "$PART_RESPONSE" id)"
require_value "id da peca de validacao" "$PART_ID" "$PART_RESPONSE"

echo "PART_ID=$PART_ID"

step "Listando pecas."

run_curl --request GET \
  --url "$APP_URL/api/parts" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando peca por ID."

run_curl --request GET \
  --url "$APP_URL/api/parts/$PART_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando peca por SKU."

run_curl --request GET \
  --url "$APP_URL/api/parts/sku/$PART_SKU" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando peca de validacao."

run_curl --request PUT \
  --url "$APP_URL/api/parts/$PART_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Filtro de Ar Validacao Atualizado\",
  \"sku\": \"$PART_SKU\",
  \"unit\": \"UNIT\",
  \"category\": \"Filtros\",
  \"brand\": \"Bosch\",
  \"costPrice\": 40.00,
  \"salePrice\": 80.00,
  \"manufacturerCode\": \"VAL-FLT-001-AT\",
  \"warrantyMonths\": 12
}"

step "Criando insumo de validacao."

SUPPLY_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/api/supplies" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Fluido Validacao\",
  \"sku\": \"$SUPPLY_SKU\",
  \"unit\": \"LITER\",
  \"category\": \"Fluidos\",
  \"brand\": \"Mobil\",
  \"costPrice\": 20.00,
  \"salePrice\": 45.00,
  \"fractionalAllowed\": true,
  \"packageSize\": 1.0
}")"

SUPPLY_ID="$(extract_json_number "$SUPPLY_RESPONSE" id)"
require_value "id do insumo de validacao" "$SUPPLY_ID" "$SUPPLY_RESPONSE"

echo "SUPPLY_ID=$SUPPLY_ID"

step "Listando insumos."

run_curl --request GET \
  --url "$APP_URL/api/supplies" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando insumo por ID."

run_curl --request GET \
  --url "$APP_URL/api/supplies/$SUPPLY_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando insumo por SKU."

run_curl --request GET \
  --url "$APP_URL/api/supplies/sku/$SUPPLY_SKU" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando insumo de validacao."

run_curl --request PUT \
  --url "$APP_URL/api/supplies/$SUPPLY_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"name\": \"Fluido Validacao Atualizado\",
  \"sku\": \"$SUPPLY_SKU\",
  \"unit\": \"LITER\",
  \"category\": \"Fluidos\",
  \"brand\": \"Mobil 1\",
  \"costPrice\": 25.00,
  \"salePrice\": 55.00,
  \"fractionalAllowed\": true,
  \"packageSize\": 1.0
}"

step "Criando estoque para peca de validacao."

run_curl --request POST \
  --url "$APP_URL/api/stocks" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"productId\": $PART_ID,
  \"quantity\": 20.00,
  \"minimumQuantity\": 5.00
}"

step "Consultando estoque por produto."

run_curl --request GET \
  --url "$APP_URL/api/stocks/product/$PART_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Listando estoques."

run_curl --request GET \
  --url "$APP_URL/api/stocks" \
  --header "Authorization: Bearer $TOKEN"

step "Registrando entrada de estoque."

run_curl --request PATCH \
  --url "$APP_URL/api/stocks/product/$PART_ID/entry" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "quantity": 5.00,
  "reason": "Entrada de validacao"
}'

step "Registrando saida de estoque."

run_curl --request PATCH \
  --url "$APP_URL/api/stocks/product/$PART_ID/exit" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "quantity": 2.00,
  "reason": "Saida de validacao"
}'

step "Atualizando estoque minimo."

run_curl --request PATCH \
  --url "$APP_URL/api/stocks/product/$PART_ID/minimum?minimumQuantity=100" \
  --header "Authorization: Bearer $TOKEN"

step "Listando estoques baixos."

run_curl --request GET \
  --url "$APP_URL/api/stocks/low" \
  --header "Authorization: Bearer $TOKEN"

step "Listando movimentacoes de estoque."

run_curl --request GET \
  --url "$APP_URL/api/stocks/product/$PART_ID/movements" \
  --header "Authorization: Bearer $TOKEN"

step "Criando ordem de servico principal."

ORDER_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/order" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --header 'correlationid: d29797dd-0eca-4ee0-918d-466ed0c8886e' \
  --data "{
  \"cpfCnpj\": \"$CLIENT_CPF\",
  \"placaVeiculo\": \"$VEHICLE_PLATE\",
  \"serviceTypes\": [
    \"TROCA_OLEO\",
    \"ALINHAMENTO\"
  ]
}")"

ORDER_ID="$(extract_json_string "$ORDER_RESPONSE" id)"
require_value "id da ordem de servico principal" "$ORDER_ID" "$ORDER_RESPONSE"

echo "ORDER_ID=$ORDER_ID"

step "Listando ordens de servico."

run_curl --request GET \
  --url "$APP_URL/order" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando ordem de servico por ID."

run_curl --request GET \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando ordem principal para EM_DIAGNOSTICO."

run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "EM_DIAGNOSTICO"
}'

step "Criando servico extra vinculado a ordem principal."

EXTRA_SERVICE_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/services" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"serviceType\": \"BALANCEAMENTO\",
  \"idOS\": \"$ORDER_ID\"
}")"

EXTRA_SERVICE_ID="$(extract_json_string "$EXTRA_SERVICE_RESPONSE" id)"
require_value "id do servico extra" "$EXTRA_SERVICE_ID" "$EXTRA_SERVICE_RESPONSE"

echo "EXTRA_SERVICE_ID=$EXTRA_SERVICE_ID"

step "Listando servicos."

run_curl --request GET \
  --url "$APP_URL/services" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando servico extra por ID."

run_curl --request GET \
  --url "$APP_URL/services/$EXTRA_SERVICE_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Listando tipos de servico."

run_curl --request GET \
  --url "$APP_URL/service-types" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando servico extra."

run_curl --request PUT \
  --url "$APP_URL/services/$EXTRA_SERVICE_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"serviceType\": \"REVISAO_GERAL\",
  \"idOS\": \"$ORDER_ID\"
}"

step "Atualizando status do servico extra para DOING."

run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DOING\",
  \"id\": \"$EXTRA_SERVICE_ID\"
}"

step "Reservando estoque para ordem principal."

run_curl --request POST \
  --url "$APP_URL/api/stocks/reservations" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"serviceOrderId\": \"$ORDER_ID\",
  \"items\": [
    {
      \"productId\": $PART_ID,
      \"quantity\": 3.5
    }
  ]
}"

step "Listando reservas da ordem principal."

run_curl --request GET \
  --url "$APP_URL/api/stocks/reservations/service-order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Consultando orcamento da ordem principal."

run_curl --request GET \
  --url "$APP_URL/api/budgets/service-order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Atualizando ordem principal para AGUARDANDO_APROVACAO."

run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "AGUARDANDO_APROVACAO"
}'

step "Cliente consulta suas ordens."

run_curl --request GET \
  --url "$APP_URL/api/clients/my-orders" \
  --header "Authorization: Bearer $CLIENT_TOKEN"

step "Cliente consulta detalhe da ordem principal."

run_curl --request GET \
  --url "$APP_URL/api/clients/my-orders/$ORDER_ID" \
  --header "Authorization: Bearer $CLIENT_TOKEN"

step "Cliente aprova ordem principal."

run_curl --request PATCH \
  --url "$APP_URL/api/clients/my-orders/$ORDER_ID/approve" \
  --header "Authorization: Bearer $CLIENT_TOKEN"

step "Mecanico visualiza servicos da ordem principal."

SERVICES_RESPONSE="$(curl --silent --show-error --fail --request GET \
  --url "$APP_URL/services/os/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN")"

echo "$SERVICES_RESPONSE"

SERVICE_IDS="$(printf '%s' "$SERVICES_RESPONSE" \
  | tr -d '\n' \
  | sed 's/[{}]/\
/g' \
  | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

service1="$(printf '%s\n' "$SERVICE_IDS" | sed -n '1p')"
service2="$(printf '%s\n' "$SERVICE_IDS" | sed -n '2p')"

require_value "id do primeiro servico da ordem principal" "$service1" "$SERVICES_RESPONSE"
require_value "id do segundo servico da ordem principal" "$service2" "$SERVICES_RESPONSE"

echo "service1=$service1"
echo "service2=$service2"

sleep $TIMEOUT_CONST
step "Mecanico inicia execucao do primeiro servico."

run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DOING\",
  \"id\": \"$service1\"
}"

sleep 2

step "Mecanico finaliza execucao do primeiro servico."

run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service1\"
}"

step "Mecanico inicia execucao do segundo servico."

run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DOING\",
  \"id\": \"$service2\"
}"

sleep 2

step "Mecanico finaliza execucao do segundo servico."

run_curl --request PATCH \
  --url "$APP_URL/services/update-status" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"status\": \"DONE\",
  \"id\": \"$service2\"
}"

step "Finalizando ordem principal."

run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "FINALIZADA"
}'

step "Confirmando reservas da ordem principal."

run_curl --request PATCH \
  --url "$APP_URL/api/stocks/reservations/service-order/$ORDER_ID/confirm" \
  --header "Authorization: Bearer $TOKEN"

step "Listando reservas confirmadas da ordem principal."

run_curl --request GET \
  --url "$APP_URL/api/stocks/reservations/service-order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Entregando veiculo ao cliente."

run_curl --request PATCH \
  --url "$APP_URL/order/$ORDER_ID" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "status": "ENTREGUE"
}'

step "Criando ordem de servico auxiliar para testar liberacao de reserva."

RELEASE_ORDER_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/order" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"cpfCnpj\": \"$CLIENT_CPF\",
  \"placaVeiculo\": \"$VEHICLE_PLATE\",
  \"serviceTypes\": [
    \"BALANCEAMENTO\"
  ]
}")"

RELEASE_ORDER_ID="$(extract_json_string "$RELEASE_ORDER_RESPONSE" id)"
require_value "id da ordem auxiliar" "$RELEASE_ORDER_ID" "$RELEASE_ORDER_RESPONSE"

echo "RELEASE_ORDER_ID=$RELEASE_ORDER_ID"

step "Reservando estoque para ordem auxiliar."

run_curl --request POST \
  --url "$APP_URL/api/stocks/reservations" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"serviceOrderId\": \"$RELEASE_ORDER_ID\",
  \"items\": [
    {
      \"productId\": $PART_ID,
      \"quantity\": 1.0
    }
  ]
}"

step "Liberando reservas da ordem auxiliar."

run_curl --request PATCH \
  --url "$APP_URL/api/stocks/reservations/service-order/$RELEASE_ORDER_ID/release" \
  --header "Authorization: Bearer $TOKEN"

step "Listando reservas liberadas da ordem auxiliar."

run_curl --request GET \
  --url "$APP_URL/api/stocks/reservations/service-order/$RELEASE_ORDER_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Monitorando tempo medio de execucao por servico."

run_curl --request POST \
  --url "$APP_URL/monitoring/all" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "timeUnit": "SECONDS"
}'

step "Monitorando tempo medio de execucao por ID."

run_curl --request POST \
  --url "$APP_URL/monitoring/by-id" \
  --header "Authorization: Bearer $TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"timeUnit\": \"SECONDS\",
  \"id\": \"$service1\"
}"

step "Desativando veiculo de validacao."

run_curl --request DELETE \
  --url "$APP_URL/api/vehicles/$VEHICLE_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Desativando cliente de validacao."

run_curl --request DELETE \
  --url "$APP_URL/api/clients/$CLIENT_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Desativando peca de validacao."

run_curl --request DELETE \
  --url "$APP_URL/api/parts/$PART_ID" \
  --header "Authorization: Bearer $TOKEN"

step "Desativando insumo de validacao."

run_curl --request DELETE \
  --url "$APP_URL/api/supplies/$SUPPLY_ID" \
  --header "Authorization: Bearer $TOKEN"

echo " "
echo " "
echo "=========================================="
echo "Validacao concluida com sucesso"
echo "=========================================="