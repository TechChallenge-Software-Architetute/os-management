package com.os.workshop.features.stock.entry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockEntryRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity,
        String reason
) {}
