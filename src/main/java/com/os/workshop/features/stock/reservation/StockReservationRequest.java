package com.os.workshop.features.stock.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StockReservationRequest(
        @NotNull UUID serviceOrderId,
        @NotEmpty @Valid List<StockReservationItemRequest> items
) {}
