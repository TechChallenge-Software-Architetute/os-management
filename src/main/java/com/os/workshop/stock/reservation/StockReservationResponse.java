package com.os.workshop.stock.reservation;

import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockReservationResponse(
        Long id,
        Long stockId,
        Long productId,
        UUID serviceOrderId,
        BigDecimal quantity,
        StockReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StockReservationResponse from(StockReservation reservation) {
        return new StockReservationResponse(
                reservation.getId(),
                reservation.getStockId(),
                reservation.getProductId(),
                reservation.getServiceOrderId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
