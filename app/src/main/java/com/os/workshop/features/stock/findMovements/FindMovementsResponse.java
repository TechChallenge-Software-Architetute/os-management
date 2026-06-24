package com.os.workshop.features.stock.findMovements;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.domain.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FindMovementsResponse(
        Long id,
        Long stockId,
        StockMovementType type,
        BigDecimal quantity,
        String reason,
        LocalDateTime createdAt
) {
    public static FindMovementsResponse from(StockMovement movement) {
        return new FindMovementsResponse(
                movement.getId(), movement.getStockId(), movement.getType(),
                movement.getQuantity(), movement.getReason(), movement.getCreatedAt()
        );
    }
}
