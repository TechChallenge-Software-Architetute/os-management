package com.os.workshop.features.stock.management;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.stock.domain.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Stock response payload.")
public record StockResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Product identifier.", example = "1") Long productId,
        @Schema(description = "Quantity.", example = "10.00") BigDecimal quantity,
        @Schema(description = "Reserved Quantity.", example = "2.00") BigDecimal reservedQuantity,
        @Schema(description = "Available Quantity.", example = "8.00") BigDecimal availableQuantity,
        @Schema(description = "Minimum Quantity.", example = "5.00") BigDecimal minimumQuantity,
        @Schema(description = "Low Stock.", example = "false") boolean lowStock,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getProductId(),
                stock.getQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity(),
                stock.getMinimumQuantity(),
                stock.isLowStock(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
}
