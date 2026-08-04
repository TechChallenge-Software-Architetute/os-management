package com.os.workshop.adapter.in.web.stock;

import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockReservationResponse(
        Long id, Long stockId, Long productId, UUID serviceOrderId,
        BigDecimal quantity, StockReservationStatus status,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static StockReservationResponse from(StockReservation reservation) {
        return new StockReservationResponse(reservation.getId(), reservation.getStockId(),
                reservation.getProductId(), reservation.getServiceOrderId(),
                reservation.getQuantity(), reservation.getStatus(),
                reservation.getCreatedAt(), reservation.getUpdatedAt());
    }
}
