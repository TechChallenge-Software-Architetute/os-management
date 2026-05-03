package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Product and quantity requested for stock reservation.")
public record StockReservationItemRequest(
        @Schema(description = "Product identifier to reserve.", example = "10")
        @NotNull Long productId,

        @Schema(description = "Quantity to reserve. Must be greater than zero.", example = "2.00")
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity
) {}
