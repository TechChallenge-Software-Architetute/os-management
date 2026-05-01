package com.os.workshop.stock.management;

import com.os.workshop.stock.domain.StockMovement;
import com.os.workshop.stock.domain.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long stockId,
        StockMovementType type,
        BigDecimal quantity,
        String reason,
        LocalDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getStockId(),
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}
