package com.os.workshop.features.stock.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateStockRequest(
        @Schema(description = "Product identifier.", example = "1")@NotNull Long productId,
        @Schema(description = "Quantity.", example = "10.00") @NotNull @DecimalMin("0.0") BigDecimal quantity,
        @Schema(description = "Minimum Quantity.", example = "5.00") @NotNull @DecimalMin("0.0") BigDecimal minimumQuantity
) {}
