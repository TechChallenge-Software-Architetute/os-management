package com.os.workshop.features.stock.management;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Stock Movement response payload.")
public record StockMovementResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Stock identifier.", example = "1") Long stockId,
        @Schema(description = "Type.", example = "CAR") StockMovementType type,
        @Schema(description = "Quantity.", example = "10.00") BigDecimal quantity,
        @Schema(description = "Reason.", example = "Stock adjustment") String reason,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt
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
