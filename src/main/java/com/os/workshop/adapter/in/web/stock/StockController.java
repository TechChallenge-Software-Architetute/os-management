package com.os.workshop.adapter.in.web.stock;

import com.os.workshop.application.stock.*;
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

    private final CreateStockUseCase createStockUseCase;
    private final FindStockByProductIdUseCase findStockByProductIdUseCase;
    private final ListStocksUseCase listStocksUseCase;
    private final StockEntryUseCase stockEntryUseCase;
    private final StockExitUseCase stockExitUseCase;
    private final UpdateMinimumUseCase updateMinimumUseCase;
    private final FindMovementsUseCase findMovementsUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final ReleaseReservationUseCase releaseReservationUseCase;
    private final FindReservationsUseCase findReservationsUseCase;

    @PostMapping
    public ResponseEntity<StockResponse> create(@Valid @RequestBody CreateStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StockResponse.from(createStockUseCase.execute(request.productId(), request.quantity(), request.minimumQuantity())));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(StockResponse.from(findStockByProductIdUseCase.execute(productId)));
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> findAll() {
        return ResponseEntity.ok(listStocksUseCase.execute().stream().map(StockResponse::from).toList());
    }

    @GetMapping("/low")
    public ResponseEntity<List<StockResponse>> findLowStock() {
        return ResponseEntity.ok(listStocksUseCase.executeLowStock().stream().map(StockResponse::from).toList());
    }

    @PatchMapping("/product/{productId}/entry")
    public ResponseEntity<StockResponse> addStock(@PathVariable Long productId, @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.ok(StockResponse.from(stockEntryUseCase.execute(productId, request.quantity(), request.reason())));
    }

    @PatchMapping("/product/{productId}/exit")
    public ResponseEntity<StockResponse> removeStock(@PathVariable Long productId, @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.ok(StockResponse.from(stockExitUseCase.execute(productId, request.quantity(), request.reason())));
    }

    @PatchMapping("/product/{productId}/minimum")
    public ResponseEntity<StockResponse> updateMinimum(@PathVariable Long productId, @RequestParam BigDecimal minimumQuantity) {
        return ResponseEntity.ok(StockResponse.from(updateMinimumUseCase.execute(productId, minimumQuantity)));
    }

    @GetMapping("/product/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(findMovementsUseCase.execute(productId).stream().map(StockMovementResponse::from).toList());
    }

    @PostMapping("/reservations")
    public ResponseEntity<List<StockReservationResponse>> reserve(@Valid @RequestBody ReserveStockRequest request) {
        var items = request.items().stream()
                .map(i -> new ReserveStockUseCase.ReserveItem(i.productId(), i.quantity())).toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reserveStockUseCase.execute(request.serviceOrderId(), items).stream()
                        .map(StockReservationResponse::from).toList());
    }

    @PatchMapping("/reservations/service-order/{serviceOrderId}/confirm")
    public ResponseEntity<List<StockReservationResponse>> confirm(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(confirmReservationUseCase.execute(serviceOrderId).stream()
                .map(StockReservationResponse::from).toList());
    }

    @PatchMapping("/reservations/service-order/{serviceOrderId}/release")
    public ResponseEntity<List<StockReservationResponse>> release(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(releaseReservationUseCase.execute(serviceOrderId).stream()
                .map(StockReservationResponse::from).toList());
    }

    @GetMapping("/reservations/service-order/{serviceOrderId}")
    public ResponseEntity<List<StockReservationResponse>> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(findReservationsUseCase.execute(serviceOrderId).stream()
                .map(StockReservationResponse::from).toList());
    }
}
