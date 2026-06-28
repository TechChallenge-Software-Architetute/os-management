package com.os.workshop.adapter.in.web.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StockExitRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero") BigDecimal quantity,
        String reason
) {}
