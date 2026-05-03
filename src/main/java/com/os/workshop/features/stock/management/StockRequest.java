package com.os.workshop.features.stock.management;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to create a stock record for a product.")
public record StockRequest(
        @Schema(description = "Product identifier associated with the stock record.", example = "10")
        @NotNull Long productId,

        @Schema(description = "Initial physical quantity in stock.", example = "15.00")
        @NotNull @DecimalMin("0.0") BigDecimal quantity,

        @Schema(description = "Minimum available quantity before low-stock warning.", example = "3.00")
        @NotNull @DecimalMin("0.0") BigDecimal minimumQuantity
) {}
