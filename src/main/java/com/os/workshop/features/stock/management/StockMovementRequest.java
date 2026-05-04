package com.os.workshop.features.stock.management;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Stock Movement request payload.")
public record StockMovementRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        @Schema(description = "Quantity.", example = "10.00") BigDecimal quantity,
        @Schema(description = "Reason.", example = "Stock adjustment") String reason
) {}
