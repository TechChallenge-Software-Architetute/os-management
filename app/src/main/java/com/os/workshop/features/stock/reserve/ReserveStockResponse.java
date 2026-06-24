package com.os.workshop.features.stock.reserve;

import com.os.workshop.features.stock.shared.domain.StockReservation;
import com.os.workshop.features.stock.shared.domain.StockReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReserveStockResponse(
        Long id,
        Long stockId,
        Long productId,
        UUID serviceOrderId,
        BigDecimal quantity,
        StockReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReserveStockResponse from(StockReservation reservation) {
        return new ReserveStockResponse(
                reservation.getId(), reservation.getStockId(), reservation.getProductId(),
                reservation.getServiceOrderId(), reservation.getQuantity(), reservation.getStatus(),
                reservation.getCreatedAt(), reservation.getUpdatedAt()
        );
    }
}
