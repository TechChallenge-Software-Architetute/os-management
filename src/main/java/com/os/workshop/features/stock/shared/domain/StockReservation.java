package com.os.workshop.features.stock.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    private Long id;
    private Long stockId;
    private Long productId;
    private UUID serviceOrderId;
    private BigDecimal quantity;
    private StockReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
