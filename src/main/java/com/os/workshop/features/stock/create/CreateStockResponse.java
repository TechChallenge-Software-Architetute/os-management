package com.os.workshop.features.stock.create;

import com.os.workshop.features.stock.shared.domain.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateStockResponse(
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
    public static CreateStockResponse from(Stock stock) {
        return new CreateStockResponse(
                stock.getId(), stock.getProductId(), stock.getQuantity(),
                stock.getReservedQuantity(), stock.getAvailableQuantity(),
                stock.getMinimumQuantity(), stock.isLowStock(),
                stock.getCreatedAt(), stock.getUpdatedAt()
        );
    }
}
