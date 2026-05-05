# Stock API — Request & Response Examples

## Stock Management

Base URL: `http://localhost:8080/api/stocks`

---

### POST /api/stocks — Criar Estoque para Produto

#### Request

```json
{
  "productId": 1,
  "quantity": 100.00,
  "minimumQuantity": 10.00
}
```

#### Response — 201 Created

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 100.00,
  "reservedQuantity": 0.00,
  "availableQuantity": 100.00,
  "minimumQuantity": 10.00,
  "lowStock": false,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

#### Response — 400 Bad Request (estoque já existe)

```json
{
  "timestamp": "2026-05-01T15:01:00",
  "status": 400,
  "error": "Stock already exists for product: 1"
}
```

---

### GET /api/stocks — Listar Todos os Estoques

#### Response — 200 OK

```json
[
  {
    "id": 1,
    "productId": 1,
    "quantity": 100.00,
    "reservedQuantity": 20.00,
    "availableQuantity": 80.00,
    "minimumQuantity": 10.00,
    "lowStock": false,
    "createdAt": "2026-05-01T15:00:00",
    "updatedAt": "2026-05-01T15:05:00"
  }
]
```

---

### GET /api/stocks/product/{productId} — Consultar Estoque por Produto

#### Response — 200 OK

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 100.00,
  "reservedQuantity": 20.00,
  "availableQuantity": 80.00,
  "minimumQuantity": 10.00,
  "lowStock": false,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:05:00"
}
```

#### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:03:00",
  "status": 400,
  "error": "Stock not found for product: 999"
}
```

---

### GET /api/stocks/low — Listar Estoques Baixos

#### Response — 200 OK

```json
[
  {
    "id": 2,
    "productId": 5,
    "quantity": 8.00,
    "reservedQuantity": 0.00,
    "availableQuantity": 8.00,
    "minimumQuantity": 10.00,
    "lowStock": true,
    "createdAt": "2026-05-01T14:00:00",
    "updatedAt": "2026-05-01T15:00:00"
  }
]
```

---

### PATCH /api/stocks/product/{productId}/entry — Entrada de Estoque

#### Request

```json
{
  "quantity": 50.00,
  "reason": "Entrega do fornecedor - NF 12345"
}
```

#### Response — 200 OK

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 150.00,
  "reservedQuantity": 20.00,
  "availableQuantity": 130.00,
  "minimumQuantity": 10.00,
  "lowStock": false,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:10:00"
}
```

---

### PATCH /api/stocks/product/{productId}/exit — Saída de Estoque

#### Request

```json
{
  "quantity": 5.00,
  "reason": "Perda por validade"
}
```

#### Response — 200 OK

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 95.00,
  "reservedQuantity": 20.00,
  "availableQuantity": 75.00,
  "minimumQuantity": 10.00,
  "lowStock": false,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:12:00"
}
```

#### Response — 409 Conflict (estoque insuficiente)

```json
{
  "timestamp": "2026-05-01T15:12:00",
  "status": 409,
  "error": "Insufficient available stock. Available: 75.00"
}
```

---

### PATCH /api/stocks/product/{productId}/minimum?minimumQuantity=15 — Atualizar Mínimo

#### Response — 200 OK

```json
{
  "id": 1,
  "productId": 1,
  "quantity": 95.00,
  "reservedQuantity": 20.00,
  "availableQuantity": 75.00,
  "minimumQuantity": 15.00,
  "lowStock": false,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:15:00"
}
```

---

### GET /api/stocks/product/{productId}/movements — Histórico de Movimentações

#### Response — 200 OK

```json
[
  {
    "id": 3,
    "stockId": 1,
    "type": "EXIT",
    "quantity": 5.00,
    "reason": "Perda por validade",
    "createdAt": "2026-05-01T15:12:00"
  },
  {
    "id": 2,
    "stockId": 1,
    "type": "ENTRY",
    "quantity": 50.00,
    "reason": "Entrega do fornecedor - NF 12345",
    "createdAt": "2026-05-01T15:10:00"
  },
  {
    "id": 1,
    "stockId": 1,
    "type": "RESERVATION",
    "quantity": 20.00,
    "reason": "Reserved for OS: 10",
    "createdAt": "2026-05-01T15:05:00"
  }
]
```

---

## Stock Reservations

Base URL: `http://localhost:8080/api/stocks/reservations`

---

### POST /api/stocks/reservations — Reservar Estoque para OS

#### Request

```json
{
  "serviceOrderId": 10,
  "items": [
    { "productId": 1, "quantity": 2.00 },
    { "productId": 5, "quantity": 3.50 }
  ]
}
```

#### Response — 201 Created

```json
[
  {
    "id": 1,
    "stockId": 1,
    "productId": 1,
    "serviceOrderId": 10,
    "quantity": 2.00,
    "status": "ACTIVE",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T15:05:00"
  },
  {
    "id": 2,
    "stockId": 2,
    "productId": 5,
    "serviceOrderId": 10,
    "quantity": 3.50,
    "status": "ACTIVE",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T15:05:00"
  }
]
```

#### Response — 409 Conflict (estoque insuficiente — operação atômica, nada é reservado)

```json
{
  "timestamp": "2026-05-01T15:05:00",
  "status": 409,
  "error": "Insufficient available stock for product 5. Available: 2.00, Requested: 3.50"
}
```

---

### PATCH /api/stocks/reservations/service-order/{serviceOrderId}/confirm — Confirmar Reservas (Serviço Concluído)

#### Response — 200 OK

```json
[
  {
    "id": 1,
    "stockId": 1,
    "productId": 1,
    "serviceOrderId": 10,
    "quantity": 2.00,
    "status": "CONFIRMED",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T16:00:00"
  },
  {
    "id": 2,
    "stockId": 2,
    "productId": 5,
    "serviceOrderId": 10,
    "quantity": 3.50,
    "status": "CONFIRMED",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T16:00:00"
  }
]
```

#### Response — 400 Bad Request (sem reservas ativas)

```json
{
  "timestamp": "2026-05-01T16:00:00",
  "status": 400,
  "error": "No active reservations found for OS: 10"
}
```

---

### PATCH /api/stocks/reservations/service-order/{serviceOrderId}/release — Liberar Reservas (OS Cancelada)

#### Response — 200 OK

```json
[
  {
    "id": 1,
    "stockId": 1,
    "productId": 1,
    "serviceOrderId": 10,
    "quantity": 2.00,
    "status": "RELEASED",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T16:05:00"
  }
]
```

#### Response — 400 Bad Request (sem reservas ativas)

```json
{
  "timestamp": "2026-05-01T16:05:00",
  "status": 400,
  "error": "No active reservations found for OS: 10"
}
```

---

### GET /api/stocks/reservations/service-order/{serviceOrderId} — Listar Reservas por OS

#### Response — 200 OK

```json
[
  {
    "id": 1,
    "stockId": 1,
    "productId": 1,
    "serviceOrderId": 10,
    "quantity": 2.00,
    "status": "CONFIRMED",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T16:00:00"
  },
  {
    "id": 2,
    "stockId": 2,
    "productId": 5,
    "serviceOrderId": 10,
    "quantity": 3.50,
    "status": "RELEASED",
    "createdAt": "2026-05-01T15:05:00",
    "updatedAt": "2026-05-01T16:05:00"
  }
]
```

---

## Enums Válidos

| Campo | Valores |
|-------|---------|
| StockMovementType | `ENTRY`, `EXIT`, `RESERVATION`, `RESERVATION_RELEASE`, `RESERVATION_CONFIRMED` |
| StockReservationStatus | `ACTIVE`, `CONFIRMED`, `RELEASED` |
