package com.os.workshop.features.stock.management;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for managing product stock.
 * <p>
 * Stock is decoupled from the product itself and managed independently.
 * Each product (Part or Supply) has a single stock record that tracks:
 * <ul>
 *   <li>{@code quantity} — total physical quantity in stock</li>
 *   <li>{@code reservedQuantity} — quantity reserved by active service orders (OS)</li>
 *   <li>{@code availableQuantity} — computed as {@code quantity - reservedQuantity} (not stored in the database)</li>
 *   <li>{@code minimumQuantity} — threshold for low stock alerts</li>
 * </ul>
 * The {@code availableQuantity} is a derived value calculated at the domain level,
 * not persisted in the database, to avoid data redundancy and inconsistency.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Creates a stock record for a product.
     * Each product can only have one stock record. The initial quantity and minimum
     * threshold are set during creation.
     *
     * @param request contains the product ID, initial quantity, and minimum quantity threshold
     * @return the created stock record with HTTP 201
     * @throws IllegalArgumentException if a stock record already exists for the given product
     */
    @PostMapping
    public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
        var stock = stockService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StockResponse.from(stock));
    }

    /**
     * Retrieves the stock record for a specific product.
     * The response includes the total quantity, reserved quantity, computed available
     * quantity, and whether the stock is below the minimum threshold.
     *
     * @param productId the UUID of the product
     * @return the stock data including availability information
     * @throws IllegalArgumentException if no stock record exists for the given product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<StockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(StockResponse.from(stockService.findByProductId(productId)));
    }

    /**
     * Lists all stock records in the system.
     * Each record includes total, reserved, and available quantities.
     *
     * @return list of all stock records
     */
    @GetMapping
    public ResponseEntity<List<StockResponse>> findAll() {
        var stocks = stockService.findAll().stream().map(StockResponse::from).toList();
        return ResponseEntity.ok(stocks);
    }

    /**
     * Lists all stock records where the available quantity is at or below the minimum threshold.
     * Useful for generating restock alerts and purchase orders.
     *
     * @return list of stock records with low available quantity
     */
    @GetMapping("/low")
    public ResponseEntity<List<StockResponse>> findLowStock() {
        var stocks = stockService.findLowStock().stream().map(StockResponse::from).toList();
        return ResponseEntity.ok(stocks);
    }

    /**
     * Registers a stock entry (adds quantity to stock).
     * A stock movement record of type ENTRY is created for audit purposes.
     * The quantity is added to the total stock, increasing the available quantity.
     *
     * @param productId the UUID of the product
     * @param request   contains the quantity to add and an optional reason
     * @return the updated stock record
     * @throws IllegalArgumentException if the quantity is not positive or stock is not found
     */
    @PatchMapping("/product/{productId}/entry")
    public ResponseEntity<StockResponse> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(StockResponse.from(stockService.addStock(productId, request)));
    }

    /**
     * Registers a stock exit (removes quantity from stock).
     * A stock movement record of type EXIT is created for audit purposes.
     * Only the available quantity (total - reserved) can be removed.
     *
     * @param productId the UUID of the product
     * @param request   contains the quantity to remove and an optional reason
     * @return the updated stock record
     * @throws IllegalArgumentException if the quantity is not positive or stock is not found
     * @throws IllegalStateException    if there is insufficient available stock
     */
    @PatchMapping("/product/{productId}/exit")
    public ResponseEntity<StockResponse> removeStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(StockResponse.from(stockService.removeStock(productId, request)));
    }

    /**
     * Updates the minimum stock threshold for a product.
     * When the available quantity falls at or below this value, the stock is flagged as low.
     *
     * @param productId       the UUID of the product
     * @param minimumQuantity the new minimum quantity threshold (must be >= 0)
     * @return the updated stock record
     * @throws IllegalArgumentException if the minimum quantity is negative or stock is not found
     */
    @PatchMapping("/product/{productId}/minimum")
    public ResponseEntity<StockResponse> updateMinimum(
            @PathVariable Long productId,
            @RequestParam BigDecimal minimumQuantity) {
        return ResponseEntity.ok(StockResponse.from(stockService.updateMinimumQuantity(productId, minimumQuantity)));
    }

    /**
     * Retrieves the full movement history for a product's stock.
     * Movements include entries, exits, reservations, reservation confirmations,
     * and reservation releases — all with timestamps and reasons.
     *
     * @param productId the UUID of the product
     * @return list of stock movements ordered by most recent first
     * @throws IllegalArgumentException if no stock record exists for the given product
     */
    @GetMapping("/product/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> findMovements(@PathVariable Long productId) {
        var movements = stockService.findMovements(productId).stream()
                .map(StockMovementResponse::from)
                .toList();
        return ResponseEntity.ok(movements);
    }
}
