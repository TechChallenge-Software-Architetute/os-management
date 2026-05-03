package com.os.workshop.features.stock.reservation;

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
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/api/stocks/reservations")
@RequiredArgsConstructor
@Tag(name = "Stock Reservations", description = "Reserve, confirm, release, and inspect stock reserved by service orders.")
public class StockReservationController {

    private static final Logger logger = LoggerFactory.getLogger(StockReservationController.class);

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
    @PostMapping
    @Operation(summary = "Reserve stock", description = "Atomically reserves products for a service order. If any item lacks available stock, no reservations are created.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock reserved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockReservationResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid reservation request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockReservationResponse>> reserve(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reservation data to create.", required = true, content = @Content(schema = @Schema(implementation = StockReservationRequest.class)))
            @Valid @RequestBody StockReservationRequest request) {
        logger.info("Reserving stock. serviceOrderId={}, itemCount={}", request.serviceOrderId(), request.items().size());
        var reservations = reservationService.reserveForServiceOrder(request).stream()
                .map(StockReservationResponse::from)
                .toList();
        logger.info("Stock reserved. serviceOrderId={}, count={}", request.serviceOrderId(), reservations.size());
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
    @PatchMapping("/service-order/{serviceOrderId}/confirm")
    @Operation(summary = "Confirm reservations", description = "Confirms all active reservations for a completed service order and consumes the reserved quantities from stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations confirmed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockReservationResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Active reservations not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockReservationResponse>> confirm(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID serviceOrderId) {
        logger.info("Confirming reservations. serviceOrderId={}", serviceOrderId);
        var confirmed = reservationService.confirmReservations(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        logger.info("Reservations confirmed. serviceOrderId={}, count={}", serviceOrderId, confirmed.size());
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
    @PatchMapping("/service-order/{serviceOrderId}/release")
    @Operation(summary = "Release reservations", description = "Releases all active reservations for a cancelled service order and returns quantities to availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations released successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockReservationResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Active reservations not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockReservationResponse>> release(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID serviceOrderId) {
        logger.info("Releasing reservations. serviceOrderId={}", serviceOrderId);
        var released = reservationService.releaseReservations(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        logger.info("Reservations released. serviceOrderId={}, count={}", serviceOrderId, released.size());
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
    @GetMapping("/service-order/{serviceOrderId}")
    @Operation(summary = "List reservations by service order", description = "Lists the full reservation history for a service order, including active, confirmed, and released reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockReservationResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service order reservations not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockReservationResponse>> findByServiceOrder(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID serviceOrderId) {
        logger.info("Listing reservations by service order. serviceOrderId={}", serviceOrderId);
        var reservations = reservationService.findByServiceOrderId(serviceOrderId).stream()
                .map(StockReservationResponse::from)
                .toList();
        logger.info("Reservations listed by service order. serviceOrderId={}, count={}", serviceOrderId, reservations.size());
        return ResponseEntity.ok(reservations);
    }
}
