package com.os.workshop.features.stock.create;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateStockRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin("0.0") BigDecimal quantity,
        @NotNull @DecimalMin("0.0") BigDecimal minimumQuantity
) {}
