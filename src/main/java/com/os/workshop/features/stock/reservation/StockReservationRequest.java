package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload used to reserve stock for a service order.")
public record StockReservationRequest(
        @Schema(description = "Service order identifier that owns the reservation.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
        @NotNull UUID serviceOrderId,

        @Schema(description = "Products and quantities to reserve.")
        @NotEmpty @Valid List<StockReservationItemRequest> items
) {}
