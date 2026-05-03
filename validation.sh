#!/bin/sh
set -e

APP_URL="${APP_URL:-http://localhost:8080}"
RUN_ID="${VALIDATION_RUN_ID:-$(date +%s)$$}"

TOKEN=""

echo "=========================================="
echo "Starting complete API validation"
echo "APP_URL=$APP_URL"
echo "RUN_ID=$RUN_ID"
echo "=========================================="

run_curl() {
  curl --silent --show-error --fail "$@"
  echo
}

request() {
  method="$1"
  url="$2"
  data="${3:-}"

  echo
  echo "------------------------------------------------------------------------------"
  echo "$method $url"
  echo "------------------------------------------------------------------------------"

  if [ -n "$data" ]; then
    RESPONSE="$(curl --silent --show-error --fail --request "$method" \
      --url "$APP_URL$url" \
      --header "Authorization: Bearer $TOKEN" \
      --header 'content-type: application/json' \
      --data "$data")"
  else
    RESPONSE="$(curl --silent --show-error --fail --request "$method" \
      --url "$APP_URL$url" \
      --header "Authorization: Bearer $TOKEN")"
  fi

  printf '%s\n' "$RESPONSE"
}

extract_string_field() {
  field="$1"
  printf '%s' "$2" | sed -n "s/.*\"$field\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p" | sed -n '1p'
}

extract_number_field() {
  field="$1"
  printf '%s' "$2" | sed -n "s/.*\"$field\"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p" | sed -n '1p'
}

extract_all_string_ids() {
  printf '%s' "$1" \
    | sed 's/[{}]/\
/g' \
    | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
}

assert_not_empty() {
  name="$1"
  value="$2"
  if [ -z "$value" ]; then
    echo "Expected $name to be present, but it was empty."
    exit 1
  fi
}

cpf_from_base() {
  base="$(printf '%09d' "$((RUN_ID % 1000000000))")"

  sum=0
  i=1
  weight=10
  while [ "$i" -le 9 ]; do
    digit="$(printf '%s' "$base" | cut -c "$i")"
    sum=$((sum + digit * weight))
    i=$((i + 1))
    weight=$((weight - 1))
  done
  rest=$((sum % 11))
  d1=$((11 - rest))
  if [ "$d1" -ge 10 ]; then d1=0; fi

  base10="$base$d1"
  sum=0
  i=1
  weight=11
  while [ "$i" -le 10 ]; do
    digit="$(printf '%s' "$base10" | cut -c "$i")"
    sum=$((sum + digit * weight))
    i=$((i + 1))
    weight=$((weight - 1))
  done
  rest=$((sum % 11))
  d2=$((11 - rest))
  if [ "$d2" -ge 10 ]; then d2=0; fi

  printf '%s%s%s' "$base" "$d1" "$d2"
}

wait_for_api() {
  i=0
  until curl -s -o /dev/null "$APP_URL"; do
    i=$((i + 1))
    if [ "$i" -ge 30 ]; then
      echo "API did not respond at $APP_URL"
      exit 1
    fi

    echo "Waiting for API..."
    sleep 1
  done
}

wait_for_api

echo
echo "------------------------------------------------------------------------------"
echo "1. Authentication"
echo "------------------------------------------------------------------------------"
LOGIN_RESPONSE="$(curl --silent --show-error --fail --request POST \
  --url "$APP_URL/auth/login" \
  --header 'content-type: application/json' \
  --data '{
  "email": "superadmin@system.com",
  "password": "coxinha123"
}')"

TOKEN="$(extract_string_field token "$LOGIN_RESPONSE")"
assert_not_empty "TOKEN" "$TOKEN"
echo "Token obtained successfully."

echo
echo "------------------------------------------------------------------------------"
echo "2. User sign-up controller"
echo "------------------------------------------------------------------------------"
NEW_USER_EMAIL="validation-$RUN_ID@example.com"
request POST "/signup" "{
  \"email\": \"$NEW_USER_EMAIL\",
  \"password\": \"Validation123!\",
  \"roles\": [\"ADMIN\"]
}"
NEW_USER_ID="$(extract_string_field id "$RESPONSE")"
assert_not_empty "NEW_USER_ID" "$NEW_USER_ID"

echo
echo "------------------------------------------------------------------------------"
echo "3. Client controller"
echo "------------------------------------------------------------------------------"
CLIENT_CPF="$(cpf_from_base)"
CLIENT_EMAIL="client-$RUN_ID@example.com"
request POST "/api/clients" "{
  \"name\": \"Validation Client\",
  \"cpf\": \"$CLIENT_CPF\",
  \"email\": \"$CLIENT_EMAIL\",
  \"phone\": \"+55 11 90000-0000\"
}"
CLIENT_ID="$(extract_number_field id "$RESPONSE")"
assert_not_empty "CLIENT_ID" "$CLIENT_ID"

request GET "/api/clients"
request GET "/api/clients/$CLIENT_ID"
request GET "/api/clients/cpf/$CLIENT_CPF"
request PUT "/api/clients/$CLIENT_ID" "{
  \"name\": \"Validation Client Updated\",
  \"cpf\": \"$CLIENT_CPF\",
  \"email\": \"client-updated-$RUN_ID@example.com\",
  \"phone\": \"+55 11 91111-1111\"
}"

echo
echo "------------------------------------------------------------------------------"
echo "4. Vehicle controller"
echo "------------------------------------------------------------------------------"
PLATE_SUFFIX="$(printf '%04d' "$((RUN_ID % 10000))")"
VEHICLE_PLATE="VLD-$PLATE_SUFFIX"
VEHICLE_UPDATED_PLATE="VLE-$PLATE_SUFFIX"

request POST "/api/vehicles" "{
  \"clientId\": $CLIENT_ID,
  \"plate\": \"$VEHICLE_PLATE\",
  \"brand\": \"Toyota\",
  \"model\": \"Corolla\",
  \"year\": 2022,
  \"color\": \"Silver\",
  \"type\": \"CAR\"
}"
VEHICLE_ID="$(extract_number_field id "$RESPONSE")"
assert_not_empty "VEHICLE_ID" "$VEHICLE_ID"

request GET "/api/vehicles/$VEHICLE_ID"
request GET "/api/vehicles/plate/$VEHICLE_PLATE"
request GET "/api/vehicles/client/$CLIENT_ID"
request PUT "/api/vehicles/$VEHICLE_ID" "{
  \"clientId\": $CLIENT_ID,
  \"plate\": \"$VEHICLE_UPDATED_PLATE\",
  \"brand\": \"Honda\",
  \"model\": \"Civic\",
  \"year\": 2023,
  \"color\": \"Black\",
  \"type\": \"CAR\"
}"

echo
echo "------------------------------------------------------------------------------"
echo "5. Parts controller"
echo "------------------------------------------------------------------------------"
PART_SKU="VAL-PART-$RUN_ID"
request POST "/api/parts" "{
  \"name\": \"Validation Brake Pad\",
  \"sku\": \"$PART_SKU\",
  \"unit\": \"UNIT\",
  \"category\": \"Brakes\",
  \"brand\": \"Bosch\",
  \"costPrice\": 45.00,
  \"salePrice\": 89.90,
  \"manufacturerCode\": \"VAL-$RUN_ID\",
  \"warrantyMonths\": 12
}"
PART_ID="$(extract_number_field id "$RESPONSE")"
assert_not_empty "PART_ID" "$PART_ID"

request GET "/api/parts"
request GET "/api/parts/$PART_ID"
request GET "/api/parts/sku/$PART_SKU"
request PUT "/api/parts/$PART_ID" "{
  \"name\": \"Validation Brake Pad Updated\",
  \"sku\": \"$PART_SKU\",
  \"unit\": \"UNIT\",
  \"category\": \"Brakes\",
  \"brand\": \"Bosch\",
  \"costPrice\": 50.00,
  \"salePrice\": 99.90,
  \"manufacturerCode\": \"VAL-UPD-$RUN_ID\",
  \"warrantyMonths\": 18
}"

echo
echo "------------------------------------------------------------------------------"
echo "6. Supplies controller"
echo "------------------------------------------------------------------------------"
SUPPLY_SKU="VAL-SUP-$RUN_ID"
request POST "/api/supplies" "{
  \"name\": \"Validation Oil\",
  \"sku\": \"$SUPPLY_SKU\",
  \"unit\": \"LITER\",
  \"category\": \"Lubricants\",
  \"brand\": \"Mobil\",
  \"costPrice\": 28.50,
  \"salePrice\": 54.90,
  \"fractionalAllowed\": true,
  \"packageSize\": 1.00
}"
SUPPLY_ID="$(extract_number_field id "$RESPONSE")"
assert_not_empty "SUPPLY_ID" "$SUPPLY_ID"

request GET "/api/supplies"
request GET "/api/supplies/$SUPPLY_ID"
request GET "/api/supplies/sku/$SUPPLY_SKU"
request PUT "/api/supplies/$SUPPLY_ID" "{
  \"name\": \"Validation Oil Updated\",
  \"sku\": \"$SUPPLY_SKU\",
  \"unit\": \"LITER\",
  \"category\": \"Lubricants\",
  \"brand\": \"Mobil\",
  \"costPrice\": 30.00,
  \"salePrice\": 59.90,
  \"fractionalAllowed\": true,
  \"packageSize\": 1.00
}"

echo
echo "------------------------------------------------------------------------------"
echo "7. Stock controller"
echo "------------------------------------------------------------------------------"
request POST "/api/stocks" "{
  \"productId\": $PART_ID,
  \"quantity\": 20.00,
  \"minimumQuantity\": 5.00
}"
STOCK_ID="$(extract_number_field id "$RESPONSE")"
assert_not_empty "STOCK_ID" "$STOCK_ID"

request GET "/api/stocks"
request GET "/api/stocks/product/$PART_ID"
request GET "/api/stocks/low"
request PATCH "/api/stocks/product/$PART_ID/entry" "{
  \"quantity\": 5.00,
  \"reason\": \"Validation stock entry\"
}"
request PATCH "/api/stocks/product/$PART_ID/exit" "{
  \"quantity\": 2.00,
  \"reason\": \"Validation stock exit\"
}"
request PATCH "/api/stocks/product/$PART_ID/minimum?minimumQuantity=4.00"
request GET "/api/stocks/product/$PART_ID/movements"

echo
echo "------------------------------------------------------------------------------"
echo "8. Service order controller"
echo "------------------------------------------------------------------------------"
request POST "/api/service-orders" '{
  "cpfCnpj": "529.982.247-25",
  "placaVeiculo": "ABC-1234",
  "serviceTypes": [
    "TROCA_OLEO",
    "ALINHAMENTO"
  ]
}'
ORDER_ID="$(extract_string_field id "$RESPONSE")"
assert_not_empty "ORDER_ID" "$ORDER_ID"
echo "ORDER_ID=$ORDER_ID"

request GET "/api/service-orders"
request GET "/api/service-orders/$ORDER_ID"
request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "EM_DIAGNOSTICO"
}'

echo
echo "------------------------------------------------------------------------------"
echo "9. Stock reservations and budget controllers"
echo "------------------------------------------------------------------------------"
request POST "/api/stocks/reservations" "{
  \"serviceOrderId\": \"$ORDER_ID\",
  \"items\": [
    {
      \"productId\": 1,
      \"quantity\": 3.50
    }
  ]
}"
request GET "/api/stocks/reservations/service-order/$ORDER_ID"
request GET "/api/budgets/service-order/$ORDER_ID"

request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "AGUARDANDO_APROVACAO"
}'
request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "APROVADO"
}'

echo
echo "------------------------------------------------------------------------------"
echo "10. Services and service types controllers"
echo "------------------------------------------------------------------------------"
request GET "/service-types"
request POST "/services" "{
  \"ServiceType\": \"TROCA_OLEO\",
  \"idOS\": \"$ORDER_ID\"
}"
CREATED_SERVICE_ID="$(extract_string_field id "$RESPONSE")"
assert_not_empty "CREATED_SERVICE_ID" "$CREATED_SERVICE_ID"
request GET "/services"
request GET "/services/service-order/$ORDER_ID"
SERVICES_RESPONSE="$RESPONSE"
SERVICE_IDS="$(extract_all_string_ids "$SERVICES_RESPONSE")"
SERVICE_1="$(printf '%s\n' "$SERVICE_IDS" | sed -n '1p')"
SERVICE_2="$(printf '%s\n' "$SERVICE_IDS" | sed -n '2p')"
assert_not_empty "SERVICE_1" "$SERVICE_1"
assert_not_empty "SERVICE_2" "$SERVICE_2"

request GET "/services/$CREATED_SERVICE_ID"
request PUT "/services/$CREATED_SERVICE_ID" "{
  \"serviceType\": \"TROCA_OLEO\",
  \"idOS\": \"$ORDER_ID\"
}"
request PATCH "/services/status" "{
  \"status\": \"DOING\",
  \"id\": \"$SERVICE_1\"
}"
sleep 2
request PATCH "/services/status" "{
  \"status\": \"DONE\",
  \"id\": \"$SERVICE_1\"
}"
request PATCH "/services/status" "{
  \"status\": \"DOING\",
  \"id\": \"$SERVICE_2\"
}"
sleep 2
request PATCH "/services/status" "{
  \"status\": \"DONE\",
  \"id\": \"$SERVICE_2\"
}"

echo
echo "------------------------------------------------------------------------------"
echo "11. Monitoring controller"
echo "------------------------------------------------------------------------------"
request POST "/monitoring/average-time" '{
  "timeUnit": "SECONDS"
}'
request POST "/monitoring/services/average-time" "{
  \"timeUnit\": \"SECONDS\",
  \"id\": \"$SERVICE_1\"
}"

echo
echo "------------------------------------------------------------------------------"
echo "12. Confirm and release reservation endpoints"
echo "------------------------------------------------------------------------------"
request PATCH "/api/stocks/reservations/service-order/$ORDER_ID/confirm"

request POST "/api/service-orders" '{
  "cpfCnpj": "529.982.247-25",
  "placaVeiculo": "ABC-1234",
  "serviceTypes": [
    "TROCA_OLEO"
  ]
}'
RELEASE_ORDER_ID="$(extract_string_field id "$RESPONSE")"
assert_not_empty "RELEASE_ORDER_ID" "$RELEASE_ORDER_ID"

request POST "/api/stocks/reservations" "{
  \"serviceOrderId\": \"$RELEASE_ORDER_ID\",
  \"items\": [
    {
      \"productId\": 1,
      \"quantity\": 1.00
    }
  ]
}"
request PATCH "/api/stocks/reservations/service-order/$RELEASE_ORDER_ID/release"

echo
echo "------------------------------------------------------------------------------"
echo "13. Complete service order lifecycle"
echo "------------------------------------------------------------------------------"
request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "EM_EXECUCAO"
}'
request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "FINALIZADA"
}'
request PATCH "/api/service-orders/$ORDER_ID" '{
  "status": "ENTREGUE"
}'

echo
echo "------------------------------------------------------------------------------"
echo "14. Delete/deactivate endpoints"
echo "------------------------------------------------------------------------------"
request DELETE "/api/vehicles/$VEHICLE_ID"
request DELETE "/api/clients/$CLIENT_ID"
request DELETE "/api/supplies/$SUPPLY_ID"
request DELETE "/api/parts/$PART_ID"

echo
echo "=========================================="
echo "Complete API validation finished successfully"
echo "=========================================="
