package com.os.workshop.features.stock.management;

import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;
import com.os.workshop.features.stock.management.StockMovementResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockMovementResponseTest {

    @Test
    void createsResponseFromMovement() {
        StockMovement movement = new StockMovement(1L, 1L, StockMovementType.ENTRY, BigDecimal.ONE, "entrada", LocalDateTime.now());

        assertEquals(StockMovementType.ENTRY, StockMovementResponse.from(movement).type());
    }
}
