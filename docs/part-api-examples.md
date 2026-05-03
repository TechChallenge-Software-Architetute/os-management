# Part API — Request & Response Examples

Base URL: `http://localhost:8080/api/parts`

---

## POST /api/parts — Criar Peça

### Request

```json
{
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12
}
```

### Response — 201 Created

```json
{
  "id": 1,
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "active": true,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (SKU duplicado)

```json
{
  "timestamp": "2026-05-01T15:02:00",
  "status": 400,
  "error": "A part with SKU 'BRK-PAD-001' already exists"
}
```

---

## GET /api/parts — Listar Peças Ativas

### Response — 200 OK

```json
[
  {
    "id": 1,
    "name": "Pastilha de Freio Dianteira",
    "sku": "BRK-PAD-001",
    "unit": "UNIT",
    "category": "Freios",
    "brand": "Bosch",
    "costPrice": 45.00,
    "salePrice": 89.90,
    "active": true,
    "manufacturerCode": "BOH-BP-2025",
    "warrantyMonths": 12,
    "createdAt": "2026-05-01T15:00:00",
    "updatedAt": "2026-05-01T15:00:00"
  }
]
```

---

## GET /api/parts/{id} — Buscar por ID

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "active": true,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:03:00",
  "status": 400,
  "error": "Part not found with id: 999"
}
```

---

## GET /api/parts/sku/{sku} — Buscar por SKU

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Pastilha de Freio Dianteira",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "active": true,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:00:00"
}
```

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:03:00",
  "status": 400,
  "error": "Part not found with SKU: INVALID-SKU"
}
```

---

## PUT /api/parts/{id} — Atualizar Peça

### Request

```json
{
  "name": "Pastilha de Freio Dianteira Cerâmica",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 55.00,
  "salePrice": 109.90,
  "manufacturerCode": "BOH-BP-2025-CER",
  "warrantyMonths": 24
}
```

### Response — 200 OK

```json
{
  "id": 1,
  "name": "Pastilha de Freio Dianteira Cerâmica",
  "sku": "BRK-PAD-001",
  "unit": "UNIT",
  "category": "Freios",
  "brand": "Bosch",
  "costPrice": 55.00,
  "salePrice": 109.90,
  "active": true,
  "manufacturerCode": "BOH-BP-2025-CER",
  "warrantyMonths": 24,
  "createdAt": "2026-05-01T15:00:00",
  "updatedAt": "2026-05-01T15:05:00"
}
```

### Response — 400 Bad Request (SKU conflita com outra peça)

```json
{
  "timestamp": "2026-05-01T15:05:00",
  "status": 400,
  "error": "A part with SKU 'EXISTING-SKU' already exists"
}
```

---

## DELETE /api/parts/{id} — Desativar Peça (Soft Delete)

### Response — 204 No Content

_(corpo vazio)_

### Response — 400 Bad Request (não encontrado)

```json
{
  "timestamp": "2026-05-01T15:06:00",
  "status": 400,
  "error": "Part not found with id: 999"
}
```

---

## Enums Válidos

| Campo | Valores aceitos |
|-------|----------------|
| unit | `UNIT`, `LITER`, `MILLILITER`, `KILOGRAM` |
