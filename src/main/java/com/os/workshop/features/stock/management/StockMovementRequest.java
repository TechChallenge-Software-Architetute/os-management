package com.os.workshop.features.stock.management;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to register a stock entry or exit movement.")
public record StockMovementRequest(
        @Schema(description = "Movement quantity. Must be greater than zero.", example = "5.00")
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @Schema(description = "Optional audit reason for the stock movement.", example = "Supplier delivery")
        String reason
) {}
