package com.os.workshop.stock.management;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockMovementRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity,
        String reason
) {}
