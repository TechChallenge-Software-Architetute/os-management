package com.os.workshop.stock.management;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin("0.0") BigDecimal quantity,
        @NotNull @DecimalMin("0.0") BigDecimal minimumQuantity
) {}
