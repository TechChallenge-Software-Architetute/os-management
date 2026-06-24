package com.os.workshop.features.stock;

import com.os.workshop.features.stock.confirmReservation.ConfirmReservationHandler;
import com.os.workshop.features.stock.create.CreateStockHandler;
import com.os.workshop.features.stock.create.CreateStockRequest;
import com.os.workshop.features.stock.create.CreateStockResponse;
import com.os.workshop.features.stock.entry.StockEntryHandler;
import com.os.workshop.features.stock.entry.StockEntryRequest;
import com.os.workshop.features.stock.exit.StockExitHandler;
import com.os.workshop.features.stock.exit.StockExitRequest;
import com.os.workshop.features.stock.findByProductId.FindStockByProductIdHandler;
import com.os.workshop.features.stock.findByProductId.FindStockByProductIdResponse;
import com.os.workshop.features.stock.findMovements.FindMovementsHandler;
import com.os.workshop.features.stock.findMovements.FindMovementsResponse;
import com.os.workshop.features.stock.findReservations.FindReservationsHandler;
import com.os.workshop.features.stock.list.ListStocksHandler;
import com.os.workshop.features.stock.list.ListStocksResponse;
import com.os.workshop.features.stock.releaseReservation.ReleaseReservationHandler;
import com.os.workshop.features.stock.reserve.ReserveStockHandler;
import com.os.workshop.features.stock.reserve.ReserveStockRequest;
import com.os.workshop.features.stock.reserve.ReserveStockResponse;
import com.os.workshop.features.stock.updateMinimum.UpdateMinimumHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Manage stock balances, movements and reservations.")
public class StockController {

    private final CreateStockHandler createStockHandler;
    private final FindStockByProductIdHandler findStockByProductIdHandler;
    private final ListStocksHandler listStocksHandler;
    private final StockEntryHandler stockEntryHandler;
    private final StockExitHandler stockExitHandler;
    private final UpdateMinimumHandler updateMinimumHandler;
    private final FindMovementsHandler findMovementsHandler;
    private final ReserveStockHandler reserveStockHandler;
    private final ConfirmReservationHandler confirmReservationHandler;
    private final ReleaseReservationHandler releaseReservationHandler;
    private final FindReservationsHandler findReservationsHandler;

    @Operation(summary = "Create stock", description = "Creates stock control for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateStockResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stock data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateStockRequest.class))
            )
            @Valid @RequestBody CreateStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateStockResponse.from(createStockHandler.handle(request)));
    }

    @Operation(summary = "Find stock by product", description = "Returns stock information for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<FindStockByProductIdResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(findStockByProductIdHandler.handle(productId)));
    }

    @Operation(summary = "List stocks", description = "Lists all stock controls.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stocks listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ListStocksResponse>> findAll() {
        return ResponseEntity.ok(listStocksHandler.handle().stream().map(ListStocksResponse::from).toList());
    }

    @Operation(summary = "List low stocks", description = "Lists stock controls below the minimum quantity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stocks listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/low")
    public ResponseEntity<List<ListStocksResponse>> findLowStock() {
        return ResponseEntity.ok(listStocksHandler.handleLowStock().stream().map(ListStocksResponse::from).toList());
    }

    @Operation(summary = "Register stock entry", description = "Adds quantity to product stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock entry registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/product/{productId}/entry")
    public ResponseEntity<FindStockByProductIdResponse> addStock(
            @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stock entry data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StockEntryRequest.class))
            )
            @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(stockEntryHandler.handle(productId, request)));
    }

    @Operation(summary = "Register stock exit", description = "Removes quantity from product stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock exit registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/product/{productId}/exit")
    public ResponseEntity<FindStockByProductIdResponse> removeStock(
            @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stock exit data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StockExitRequest.class))
            )
            @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(stockExitHandler.handle(productId, request)));
    }

    @Operation(summary = "Update minimum stock", description = "Updates the minimum quantity for a product stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Minimum stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/product/{productId}/minimum")
    public ResponseEntity<FindStockByProductIdResponse> updateMinimum(
            @PathVariable Long productId, @RequestParam BigDecimal minimumQuantity) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(
                updateMinimumHandler.handle(productId, minimumQuantity)));
    }

    @Operation(summary = "List stock movements", description = "Lists stock movements for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock movements listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/{productId}/movements")
    public ResponseEntity<List<FindMovementsResponse>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(findMovementsHandler.handle(productId).stream()
                .map(FindMovementsResponse::from).toList());
    }

    // ==================== Reservation Endpoints ====================

    @Operation(summary = "Reserve stock", description = "Reserves stock items for a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reservations")
    public ResponseEntity<List<ReserveStockResponse>> reserve(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stock reservation data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReserveStockRequest.class))
            )
            @Valid @RequestBody ReserveStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reserveStockHandler.handle(request).stream()
                        .map(ReserveStockResponse::from).toList());
    }

    @Operation(summary = "Confirm reservations", description = "Confirms stock reservations for a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/reservations/service-order/{serviceOrderId}/confirm")
    public ResponseEntity<List<ReserveStockResponse>> confirm(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(confirmReservationHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }

    @Operation(summary = "Release reservations", description = "Releases stock reservations for a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/reservations/service-order/{serviceOrderId}/release")
    public ResponseEntity<List<ReserveStockResponse>> release(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(releaseReservationHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }

    @Operation(summary = "Find reservations", description = "Lists stock reservations for a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/reservations/service-order/{serviceOrderId}")
    public ResponseEntity<List<ReserveStockResponse>> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(findReservationsHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }
}
