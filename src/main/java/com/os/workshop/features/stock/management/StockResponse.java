package com.os.workshop.features.stock.management;

import com.os.workshop.features.stock.domain.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        Long productId,
        BigDecimal quantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        BigDecimal minimumQuantity,
        boolean lowStock,
        LocalDateTime createdAt,
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
