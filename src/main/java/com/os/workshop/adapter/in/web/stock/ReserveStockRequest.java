package com.os.workshop.adapter.in.web.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReserveStockRequest(
        @NotNull UUID serviceOrderId,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            @NotNull @DecimalMin(value = "0.01", message = "Quantity must be greater than zero") BigDecimal quantity
    ) {}
}
