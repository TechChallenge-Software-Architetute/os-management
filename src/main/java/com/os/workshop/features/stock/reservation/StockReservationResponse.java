package com.os.workshop.features.stock.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Stock Reservation response payload.")
public record StockReservationResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Stock identifier.", example = "1") Long stockId,
        @Schema(description = "Product identifier.", example = "1") Long productId,
        @Schema(description = "Service Order identifier.", example = "11111111-1111-1111-1111-111111111111") UUID serviceOrderId,
        @Schema(description = "Quantity.", example = "10.00") BigDecimal quantity,
        @Schema(description = "Status.", example = "DOING") StockReservationStatus status,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
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
