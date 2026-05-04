package com.os.workshop.features.stock.shared.mapper;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.domain.StockMovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockMovementMapperTest {

    private final StockMovementMapper mapper = new StockMovementMapperImpl();

    @Test
    void convertsMovement() {
        StockMovement movement = new StockMovement(1L, 1L, StockMovementType.ENTRY, BigDecimal.ONE, "entrada", LocalDateTime.now());
        assertEquals(StockMovementType.ENTRY, mapper.toDomain(mapper.toEntity(movement)).getType());
    }
}
