package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Stock Reservation request payload.")
public record StockReservationRequest(
        @Schema(description = "Service Order identifier.", example = "11111111-1111-1111-1111-111111111111") @NotNull UUID serviceOrderId,
        @Schema(description = "Items.", example = "[]") @NotEmpty @Valid List<StockReservationItemRequest> items
) {}
