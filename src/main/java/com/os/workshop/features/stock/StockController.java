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

    @PostMapping
    public ResponseEntity<CreateStockResponse> create(@Valid @RequestBody CreateStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateStockResponse.from(createStockHandler.handle(request)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<FindStockByProductIdResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(findStockByProductIdHandler.handle(productId)));
    }

    @GetMapping
    public ResponseEntity<List<ListStocksResponse>> findAll() {
        return ResponseEntity.ok(listStocksHandler.handle().stream().map(ListStocksResponse::from).toList());
    }

    @GetMapping("/low")
    public ResponseEntity<List<ListStocksResponse>> findLowStock() {
        return ResponseEntity.ok(listStocksHandler.handleLowStock().stream().map(ListStocksResponse::from).toList());
    }

    @PatchMapping("/product/{productId}/entry")
    public ResponseEntity<FindStockByProductIdResponse> addStock(
            @PathVariable Long productId, @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(stockEntryHandler.handle(productId, request)));
    }

    @PatchMapping("/product/{productId}/exit")
    public ResponseEntity<FindStockByProductIdResponse> removeStock(
            @PathVariable Long productId, @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(stockExitHandler.handle(productId, request)));
    }

    @PatchMapping("/product/{productId}/minimum")
    public ResponseEntity<FindStockByProductIdResponse> updateMinimum(
            @PathVariable Long productId, @RequestParam BigDecimal minimumQuantity) {
        return ResponseEntity.ok(FindStockByProductIdResponse.from(
                updateMinimumHandler.handle(productId, minimumQuantity)));
    }

    @GetMapping("/product/{productId}/movements")
    public ResponseEntity<List<FindMovementsResponse>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(findMovementsHandler.handle(productId).stream()
                .map(FindMovementsResponse::from).toList());
    }

    // ==================== Reservation Endpoints ====================

    @PostMapping("/reservations")
    public ResponseEntity<List<ReserveStockResponse>> reserve(
            @Valid @RequestBody ReserveStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reserveStockHandler.handle(request).stream()
                        .map(ReserveStockResponse::from).toList());
    }

    @PatchMapping("/reservations/service-order/{serviceOrderId}/confirm")
    public ResponseEntity<List<ReserveStockResponse>> confirm(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(confirmReservationHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }

    @PatchMapping("/reservations/service-order/{serviceOrderId}/release")
    public ResponseEntity<List<ReserveStockResponse>> release(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(releaseReservationHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }

    @GetMapping("/reservations/service-order/{serviceOrderId}")
    public ResponseEntity<List<ReserveStockResponse>> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(findReservationsHandler.handle(serviceOrderId).stream()
                .map(ReserveStockResponse::from).toList());
    }
}
