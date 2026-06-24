package com.os.workshop.features.stock.findByProductId;

import com.os.workshop.features.stock.shared.domain.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FindStockByProductIdResponse(
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
    public static FindStockByProductIdResponse from(Stock stock) {
        return new FindStockByProductIdResponse(
                stock.getId(), stock.getProductId(), stock.getQuantity(),
                stock.getReservedQuantity(), stock.getAvailableQuantity(),
                stock.getMinimumQuantity(), stock.isLowStock(),
                stock.getCreatedAt(), stock.getUpdatedAt()
        );
    }
}
