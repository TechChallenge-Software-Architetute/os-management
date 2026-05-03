package com.os.workshop.features.stock.management;

import com.os.workshop.features.stock.domain.Stock;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Stock record returned by the API.")
public record StockResponse(
        @Schema(description = "Stock unique identifier.", example = "1")
        Long id,
        @Schema(description = "Product identifier associated with the stock record.", example = "10")
        Long productId,
        @Schema(description = "Total physical quantity in stock.", example = "15.00")
        BigDecimal quantity,
        @Schema(description = "Quantity reserved for active service orders.", example = "2.00")
        BigDecimal reservedQuantity,
        @Schema(description = "Available quantity calculated as total minus reserved.", example = "13.00")
        BigDecimal availableQuantity,
        @Schema(description = "Minimum available quantity before low-stock warning.", example = "3.00")
        BigDecimal minimumQuantity,
        @Schema(description = "Whether available quantity is at or below the minimum quantity.", example = "false")
        boolean lowStock,
        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
        LocalDateTime updatedAt
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
