package com.os.workshop.features.stock.management;

import com.os.workshop.features.common.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Stock", description = "Manage product stock, availability, thresholds, and stock movement history.")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

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
    @Operation(summary = "Create stock record", description = "Creates the stock record for a product. Each product can have only one stock record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock record created successfully", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or stock already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Stock data to create.", required = true, content = @Content(schema = @Schema(implementation = StockRequest.class)))
            @Valid @RequestBody StockRequest request) {
        logger.info("Creating stock. productId={}", request.productId());
        var stock = stockService.create(request);
        var response = StockResponse.from(stock);
        logger.info("Stock created. id={}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @Operation(summary = "Find stock by product", description = "Retrieves stock availability and threshold information for a product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock record found", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> findByProductId(
            @Parameter(description = "Product unique identifier.", example = "10", required = true)
            @PathVariable Long productId) {
        logger.info("Finding stock by product. productId={}", productId);
        return ResponseEntity.ok(StockResponse.from(stockService.findByProductId(productId)));
    }

    /**
     * Lists all stock records in the system.
     * Each record includes total, reserved, and available quantities.
     *
     * @return list of all stock records
     */
    @GetMapping
    @Operation(summary = "List stock records", description = "Lists all stock records with total, reserved, available, and minimum quantities.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock records listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockResponse>> findAll() {
        logger.info("Listing stock records.");
        var stocks = stockService.findAll().stream().map(StockResponse::from).toList();
        logger.info("Stock records listed. count={}", stocks.size());
        return ResponseEntity.ok(stocks);
    }

    /**
     * Lists all stock records where the available quantity is at or below the minimum threshold.
     * Useful for generating restock alerts and purchase orders.
     *
     * @return list of stock records with low available quantity
     */
    @GetMapping("/low")
    @Operation(summary = "List low stock records", description = "Lists stock records whose available quantity is at or below the configured minimum quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Low stock records listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockResponse>> findLowStock() {
        logger.info("Listing low stock records.");
        var stocks = stockService.findLowStock().stream().map(StockResponse::from).toList();
        logger.info("Low stock records listed. count={}", stocks.size());
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
    @Operation(summary = "Register stock entry", description = "Adds quantity to stock and records an ENTRY movement for audit history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock entry registered successfully", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid movement request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> addStock(
            @Parameter(description = "Product unique identifier.", example = "10", required = true)
            @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entry movement data.", required = true, content = @Content(schema = @Schema(implementation = StockMovementRequest.class)))
            @Valid @RequestBody StockMovementRequest request) {
        logger.info("Adding stock. productId={}, quantity={}", productId, request.quantity());
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
    @Operation(summary = "Register stock exit", description = "Removes available quantity from stock and records an EXIT movement for audit history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock exit registered successfully", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid movement request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> removeStock(
            @Parameter(description = "Product unique identifier.", example = "10", required = true)
            @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Exit movement data.", required = true, content = @Content(schema = @Schema(implementation = StockMovementRequest.class)))
            @Valid @RequestBody StockMovementRequest request) {
        logger.info("Removing stock. productId={}, quantity={}", productId, request.quantity());
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
    @Operation(summary = "Update minimum stock", description = "Updates the minimum quantity threshold used to flag low stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Minimum quantity updated successfully", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid minimum quantity", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> updateMinimum(
            @Parameter(description = "Product unique identifier.", example = "10", required = true)
            @PathVariable Long productId,
            @Parameter(description = "New minimum quantity threshold.", example = "3.00", required = true)
            @RequestParam BigDecimal minimumQuantity) {
        logger.info("Updating minimum stock. productId={}, minimumQuantity={}", productId, minimumQuantity);
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
    @Operation(summary = "List stock movements", description = "Retrieves the full stock movement history for a product, ordered by most recent first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockMovementResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid product identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockMovementResponse>> findMovements(
            @Parameter(description = "Product unique identifier.", example = "10", required = true)
            @PathVariable Long productId) {
        logger.info("Listing stock movements. productId={}", productId);
        var movements = stockService.findMovements(productId).stream()
                .map(StockMovementResponse::from)
                .toList();
        logger.info("Stock movements listed. productId={}, count={}", productId, movements.size());
        return ResponseEntity.ok(movements);
    }
}
