# Budget API — Request & Response Examples

Base URL: `http://localhost:8080/api/budgets`

O orçamento é gerado automaticamente a partir das reservas de estoque ativas de uma Ordem de Serviço. Não existe endpoint de criação — o budget é recalculado sempre que uma reserva é criada, confirmada ou liberada.

---

## GET /api/budgets/service-order/{serviceOrderId} — Consultar Orçamento por OS

### Response — 200 OK (orçamento com itens)

```json
{
  "id": 1,
  "serviceOrderId": "a46ac51b-5ca6-439b-ba52-a36bd52e8647",
  "totalPrice": 329.70,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Pastilha de Freio Dianteira",
      "productSku": "BRK-PAD-001",
      "productType": "PART",
      "quantity": 2.00,
      "unitPrice": 89.90,
      "totalPrice": 179.80
    },
    {
      "id": 2,
      "productId": 5,
      "productName": "Óleo Motor 5W30 Sintético",
      "productSku": "OIL-5W30-SINT",
      "productType": "SUPPLY",
      "quantity": 3.00,
      "unitPrice": 49.90,
      "totalPrice": 149.70
    }
  ],
  "createdAt": "2026-05-02T10:00:00",
  "updatedAt": "2026-05-02T10:05:00"
}
```

### Response — 404 Not Found (OS sem reservas/orçamento)

_(corpo vazio)_

---

## Fluxo Completo

1. Criar peças e insumos via `/api/parts` e `/api/supplies`
2. Criar estoque para cada produto via `POST /api/stocks`
3. Criar reservas para a OS via `POST /api/stocks/reservations`
4. Consultar orçamento via `GET /api/budgets/service-order/{serviceOrderId}`

O orçamento é recalculado automaticamente quando:
- Novas reservas são criadas para a OS
- Reservas são confirmadas (serviço concluído)
- Reservas são liberadas (OS cancelada)

---

## Campos do Orçamento

| Campo | Descrição |
|-------|-----------|
| totalPrice | Soma de todos os `totalPrice` dos itens |
| items[].unitPrice | Snapshot do `salePrice` do produto no momento do cálculo |
| items[].totalPrice | `quantity × unitPrice` |
| items[].productType | `PART` ou `SUPPLY` |
