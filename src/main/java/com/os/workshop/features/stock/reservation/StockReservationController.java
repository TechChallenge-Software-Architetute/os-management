package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing stock reservations tied to service orders (OS).
 * <p>
 * When a service order is created, the parts and supplies required for each service
 * are reserved in stock. Reserved products remain physically in stock but are not
 * available for other service orders.
 * <p>
 * Reservation lifecycle:
 * <ul>
 *   <li>ACTIVE — products are reserved, unavailable for other OS</li>
 *   <li>CONFIRMED — service completed, products are actually consumed from stock</li>
 *   <li>RELEASED — OS cancelled, products become available again</li>
 * </ul>
 */
@Tag(name = "Stock Reservations", description = "Manage stock reservations for service orders.")
@RestController
@RequestMapping("/api/stocks/reservations")
@RequiredArgsConstructor
public class StockReservationController {

    private final StockReservationService reservationService;

    /**
     * Reserves stock for a service order.
     * <p>
     * This is an all-or-nothing (atomic) operation: if any product in the list does not
     * have enough available stock, the entire reservation fails and no products are reserved.
     * This prevents partial reservations that could leave an OS incomplete.
     * <p>
     * Each reserved product increases the {@code reservedQuantity} on the stock record,
     * reducing the {@code availableQuantity} for other service orders.
     *
     * @param request contains the service order ID and a list of products with quantities to reserve
     * @return list of created reservations with ACTIVE status and HTTP 201
     * @throws IllegalArgumentException if stock is not found for any product
     * @throws IllegalStateException    if any product has insufficient available stock
     */
    @Operation(summary = "Reserve stock", description = "Reserve stock endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<List<StockReservationResponse>> reserve(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(schema = @Schema(implementation = StockReservationRequest.class))

            )

            @Valid @RequestBody StockReservationRequest request) {
        var reservations = reservationService.reserveForServiceOrder(request).stream()
                .map(StockReservationResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(reservations);
    }

    /**
     * Confirms all active reservations for a service order.
     * <p>
     * Called when the service is completed and the products were actually used.
     * This deducts the reserved quantities from the total stock and changes
     * the reservation status from ACTIVE to CONFIRMED.
     * A RESERVATION_CONFIRMED movement is recorded for each product.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of confirmed reservations
     * @throws IllegalArgumentException if no active reservations exist for the given OS
     */
    @Operation(summary = "Confirm reservations", description = "Confirm reservations endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/service-order/{serviceOrderId}/confirm")
    public ResponseEntity<List<StockReservationResponse>> confirm(@PathVariable UUID serviceOrderId) {
        var confirmed = reservationService.confirmReservations(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        return ResponseEntity.ok(confirmed);
    }

    /**
     * Releases all active reservations for a service order.
     * <p>
     * Called when the service order is cancelled. The reserved quantities are returned
     * to the available pool, and the reservation status changes from ACTIVE to RELEASED.
     * A RESERVATION_RELEASE movement is recorded for each product.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of released reservations
     * @throws IllegalArgumentException if no active reservations exist for the given OS
     */
    @Operation(summary = "Release reservations", description = "Release reservations endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/service-order/{serviceOrderId}/release")
    public ResponseEntity<List<StockReservationResponse>> release(@PathVariable UUID serviceOrderId) {
        var released = reservationService.releaseReservations(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        return ResponseEntity.ok(released);
    }

    /**
     * Lists all reservations (of any status) for a service order.
     * Useful for viewing the full reservation history of an OS, including
     * active, confirmed, and released reservations.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of all reservations for the given OS
     */
    @Operation(summary = "Find budget by service order", description = "Find budget by service order endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<List<StockReservationResponse>> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        var reservations = reservationService.findByServiceOrderId(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservations);
    }
}
