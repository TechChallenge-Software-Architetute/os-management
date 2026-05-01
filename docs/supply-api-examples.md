# Supply API — Request & Response Examples

Base URL: `http://localhost:8080/api/supplies`

---

## POST /api/supplies — Criar Insumo

### Request

```json
{
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "fractionalAllowed": true,
  "packageSize": 1.0
}
```

### Response — 201 Created

```json
{
  "id": 1,
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "active": true,
  "fractionalAllowed": true,
  "packageSize": 1.0,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (SKU duplicado)

```json
{
  "timestamp": "2026-05-01T15:02:00",
  "status": 400,
  "error": "A supply with SKU 'OIL-5W30-SINT' already exists"
}
```

---

## GET /api/supplies — Listar Insumos Ativos

### Response — 200 OK

```json
[
  {
    "id": 1,
    "name": "Óleo Motor 5W30 Sintético",
    "sku": "OIL-5W30-SINT",
    "unit": "LITER",
    "category": "Lubrificantes",
    "brand": "Mobil",
    "costPrice": 28.50,
    "salePrice": 54.90,
    "active": true,
    "fractionalAllowed": true,
    "packageSize": 1.0,
    "createdAt": "2026-05-01T15:00:00",
    "updatedAt": "2026-05-01T15:00:00"
  }
]
```

---

## GET /api/supplies/{id} — Buscar por ID

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "active": true,
  "fractionalAllowed": true,
  "packageSize": 1.0,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:03:00",
  "status": 400,
  "error": "Supply not found with id: 999"
}
```

---

## GET /api/supplies/sku/{sku} — Buscar por SKU

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Óleo Motor 5W30 Sintético",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil",
  "costPrice": 28.50,
  "salePrice": 54.90,
  "active": true,
  "fractionalAllowed": true,
  "packageSize": 1.0,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:03:00",
  "status": 400,
  "error": "Supply not found with SKU: INVALID-SKU"
}
```

---

## PUT /api/supplies/{id} — Atualizar Insumo

### Request

```json
{
  "name": "Óleo Motor 5W30 Sintético Premium",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil 1",
  "costPrice": 32.00,
  "salePrice": 62.90,
  "fractionalAllowed": true,
  "packageSize": 1.0
}
```

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Óleo Motor 5W30 Sintético Premium",
  "sku": "OIL-5W30-SINT",
  "unit": "LITER",
  "category": "Lubrificantes",
  "brand": "Mobil 1",
  "costPrice": 32.00,
  "salePrice": 62.90,
  "active": true,
  "fractionalAllowed": true,
  "packageSize": 1.0,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:05:00"
}
```

### Response — 400 Bad Request (SKU conflita com outro insumo)

```json
{
  "timestamp": "2026-05-01T15:05:00",
  "status": 400,
  "error": "A supply with SKU 'EXISTING-SKU' already exists"
}
```

---

## DELETE /api/supplies/{id} — Desativar Insumo (Soft Delete)

### Response — 204 No Content

_(corpo vazio)_

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:06:00",
  "status": 400,
  "error": "Supply not found with id: 999"
}
```

---

## Enums Válidos

| Campo | Valores aceitos |
|-------|----------------|
| unit | `UNIT`, `LITER`, `MILLILITER`, `KILOGRAM` |
