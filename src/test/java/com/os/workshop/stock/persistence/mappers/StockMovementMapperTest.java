package com.os.workshop.stock.persistence.mappers;

import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;
import com.os.workshop.features.stock.persistence.mappers.StockMovementMapper;
import com.os.workshop.features.stock.persistence.mappers.StockMovementMapperImpl;
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
