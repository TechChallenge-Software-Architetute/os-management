package com.os.workshop.stock.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    private Long id;
    private Long stockId;
    private Long productId;
    private Long serviceOrderId;
    private BigDecimal quantity;
    private StockReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
