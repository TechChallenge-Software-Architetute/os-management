#!/bin/bash
set -e

echo "=========================================="
echo "Iniciando validacao da API"
echo "=========================================="

timeout 10s bash -c "until curl -s http://localhost:8080/health; do echo 'Aguardando API...'; sleep 1; done"

curl --request POST \
  --url "http://localhost:8080/order" \
  --header 'content-type: application/json' \
  --header 'correlationid: a8ce810f-5919-41f9-a677-2e1f1987cfa1' \
  --data '{
  "cpfCnpj": "529.982.247-25",
  "placaVeiculo": "ABC-1234",
  "serviceTypes": [
    "TROCA_OLEO",
    "ALINHAMENTO",
    "REPARO_FREIOS"
  ]
}'
