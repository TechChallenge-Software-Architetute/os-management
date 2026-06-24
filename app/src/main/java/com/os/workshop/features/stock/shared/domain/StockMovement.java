package com.os.workshop.features.stock.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    private Long id;
    private Long stockId;
    private StockMovementType type;
    private BigDecimal quantity;
    private String reason;
    private LocalDateTime createdAt;
}
