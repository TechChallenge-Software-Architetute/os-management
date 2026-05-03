package com.os.workshop.features.stock.management;

import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Stock movement history item returned by the API.")
public record StockMovementResponse(
        @Schema(description = "Stock movement unique identifier.", example = "1")
        Long id,
        @Schema(description = "Stock identifier associated with this movement.", example = "1")
        Long stockId,
        @Schema(description = "Movement type.", example = "ENTRY")
        StockMovementType type,
        @Schema(description = "Movement quantity.", example = "5.00")
        BigDecimal quantity,
        @Schema(description = "Audit reason for the stock movement.", example = "Supplier delivery")
        String reason,
        @Schema(description = "Movement creation date and time.", example = "2026-05-03T12:30:00")
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
