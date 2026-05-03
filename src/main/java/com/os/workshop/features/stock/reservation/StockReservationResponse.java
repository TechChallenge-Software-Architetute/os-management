package com.os.workshop.features.stock.reservation;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Stock reservation data returned by the API.")
public record StockReservationResponse(
        @Schema(description = "Reservation unique identifier.", example = "1")
        Long id,

        @Schema(description = "Stock identifier associated with this reservation.", example = "1")
        Long stockId,

        @Schema(description = "Reserved product identifier.", example = "10")
        Long productId,

        @Schema(description = "Service order identifier that owns the reservation.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
        UUID serviceOrderId,

        @Schema(description = "Reserved quantity.", example = "2.00")
        BigDecimal quantity,

        @Schema(description = "Reservation lifecycle status.", example = "ACTIVE")
        StockReservationStatus status,

        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
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
