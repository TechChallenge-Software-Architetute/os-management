package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Stock Reservation Item request payload.")
public record StockReservationItemRequest(
        @Schema(description = "Product identifier.", example = "1") @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        @Schema(description = "Quantity.", example = "10.00") BigDecimal quantity
) {}
