# os-management
Tech challenge - Phase 1


# Build Project Image
```
docker build -t os-management .
```
# Running the container and mapping ports

```
docker run -p 8080:8080 -td os-management
```
# Building and running the project at once
```
docker-compose up -d
```
This will build and create both postgres and project image. Mapping the os-management port to 8080 and postgres to 5432.

Connection URL: `jdbc:postgresql://localhost:5432/workshop`

### SonarQube

The Docker Compose file also starts SonarQube at http://localhost:9000.

To initialize SonarQube, create the `os-management` project, generate a token, and run the Maven analysis with coverage:

```
docker compose up sonar_scan
```

The bootstrap service uses `Giovanni123*` as the SonarQube admin password, creates the project, stores the generated token in a Docker volume, and the Maven scanner uses it automatically.

You can override the defaults when running the compose command:

```
SONAR_ADMIN_PASSWORD=my-password SONAR_PROJECT_KEY=os-management docker compose --profile sonar up sonar_scan
```

If your local SonarQube volume was already configured manually, provide the current admin password or an existing token:

```
SONAR_ADMIN_PASSWORD=current-password docker compose up sonar_scan
```

```
SONAR_TOKEN=sqp_xxx docker compose up sonar_scan
```

JaCoCo generates the coverage XML at `target/site/jacoco/jacoco.xml`, and the Sonar Maven scanner sends it to SonarQube.

### Swagger

This project uses OpenAPI as documentation, there you can find all endpoints of the app.
To access Swagger UI and see the endpoints click here -> [Swagger](http://localhost:8080/swagger-ui/index.html#/).

---

## API Endpoints

### Parts — `/api/parts`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/parts` | Create a new part. SKU must be unique. ID is auto-generated. |
| GET | `/api/parts` | List all active parts. Deactivated parts are excluded. |
| GET | `/api/parts/{id}` | Get a part by its UUID. |
| PUT | `/api/parts/{id}` | Update a part. SKU uniqueness is validated if changed. |
| DELETE | `/api/parts/{id}` | Deactivate a part (soft delete). The part remains in the database but is excluded from listings. |

**POST/PUT request body example:**
```json
{
  "name": "Brake Pad",
  "sku": "BP-001",
  "unit": "UNIT",
  "category": "Brakes",
  "brand": "Bosch",
  "costPrice": 45.00,
  "salePrice": 89.90,
  "manufacturerCode": "BOH-BP-2025",
  "warrantyMonths": 12
}
```

---

### Supplies — `/api/supplies`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/supplies` | Create a new supply. SKU must be unique. ID is auto-generated. |
| GET | `/api/supplies` | List all active supplies. Deactivated supplies are excluded. |
| GET | `/api/supplies/{id}` | Get a supply by its UUID. |
| PUT | `/api/supplies/{id}` | Update a supply. SKU uniqueness is validated if changed. |
| DELETE | `/api/supplies/{id}` | Deactivate a supply (soft delete). |

**POST/PUT request body example:**
```json
{
  "name": "Engine Oil 5W30",
  "sku": "OIL-5W30",
  "unit": "LITER",
  "category": "Lubricants",
  "brand": "Mobil",
  "costPrice": 25.00,
  "salePrice": 49.90,
  "fractionalAllowed": true,
  "packageSize": 1.0
}
```

---

### Stock — `/api/stocks`

Stock is managed independently from products. Each product has a single stock record tracking:
- `quantity` — total physical quantity in stock
- `reservedQuantity` — quantity reserved by active service orders
- `availableQuantity` — computed as `quantity - reservedQuantity` (not stored in the database)
- `minimumQuantity` — threshold for low stock alerts

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/stocks` | Create a stock record for a product. Each product can only have one stock record. |
| GET | `/api/stocks` | List all stock records with total, reserved, and available quantities. |
| GET | `/api/stocks/low` | List stocks where available quantity is at or below the minimum threshold. |
| GET | `/api/stocks/product/{productId}` | Get stock for a specific product. |
| PATCH | `/api/stocks/product/{productId}/entry` | Add stock (entry). Increases total and available quantity. Records an ENTRY movement. |
| PATCH | `/api/stocks/product/{productId}/exit` | Remove stock (exit). Only available quantity can be removed. Records an EXIT movement. |
| PATCH | `/api/stocks/product/{productId}/minimum?minimumQuantity=X` | Update the minimum stock threshold. |
| GET | `/api/stocks/product/{productId}/movements` | Get full movement history (entries, exits, reservations, confirmations, releases). |

**POST (create stock) request body:**
```json
{
  "productId": "uuid-of-the-product",
  "quantity": 100,
  "minimumQuantity": 10
}
```

**PATCH (entry/exit) request body:**
```json
{
  "quantity": 25,
  "reason": "Supplier delivery"
}
```

---

### Stock Reservations — `/api/stocks/reservations`

Reservations are created when a service order (OS) requires parts or supplies. Reserved products remain physically in stock but are unavailable for other service orders.

**Reservation lifecycle:**
- `ACTIVE` — products reserved, unavailable for other OS
- `CONFIRMED` — service completed, products actually consumed from stock
- `RELEASED` — OS cancelled, products returned to available pool

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/stocks/reservations` | Reserve stock for a service order. Atomic operation: if any item has insufficient stock, nothing is reserved. |
| PATCH | `/api/stocks/reservations/service-order/{serviceOrderId}/confirm` | Confirm all active reservations (service completed). Deducts from total stock. |
| PATCH | `/api/stocks/reservations/service-order/{serviceOrderId}/release` | Release all active reservations (OS cancelled). Returns products to available pool. |
| GET | `/api/stocks/reservations/service-order/{serviceOrderId}` | List all reservations (any status) for a service order. |

**POST (reserve) request body:**
```json
{
  "serviceOrderId": "uuid-of-the-service-order",
  "items": [
    { "productId": "uuid-of-part", "quantity": 2 },
    { "productId": "uuid-of-supply", "quantity": 3.5 }
  ]
}
```

---

### Enums Reference

| Enum | Values |
|------|--------|
| UnitOfMeasure | `UNIT`, `LITER`, `MILLILITER`, `KILOGRAM` |
| ProductType | `PART`, `SUPPLY` |
| StockMovementType | `ENTRY`, `EXIT`, `RESERVATION`, `RESERVATION_RELEASE`, `RESERVATION_CONFIRMED` |
| StockReservationStatus | `ACTIVE`, `CONFIRMED`, `RELEASED` |
