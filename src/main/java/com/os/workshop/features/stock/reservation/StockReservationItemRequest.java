package com.os.workshop.features.stock.reservation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockReservationItemRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity
) {}
