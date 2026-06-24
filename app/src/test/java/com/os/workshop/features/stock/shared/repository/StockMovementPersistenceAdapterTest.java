package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.domain.StockMovementType;
import com.os.workshop.features.stock.shared.mapper.StockMovementMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementPersistenceAdapterTest {

    @Mock private StockMovementJpaRepository jpaRepository;
    @Mock private StockMovementMapper mapper;
    @InjectMocks private StockMovementPersistenceAdapter adapter;

    @Test
    void savesAndFindsMovements() {
        StockMovement movement = new StockMovement(1L, 1L, StockMovementType.ENTRY, BigDecimal.ONE, "entrada", LocalDateTime.now());
        StockMovementEntity entity = new StockMovementEntity();

        when(mapper.toEntity(movement)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(movement);
        when(jpaRepository.findByStockIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entity));

        assertEquals(1L, adapter.save(movement).getId());
        assertEquals(1, adapter.findByStockId(1L).size());
    }
}
