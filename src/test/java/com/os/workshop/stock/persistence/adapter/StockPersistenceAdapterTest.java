package com.os.workshop.stock.persistence.adapter;

import com.os.workshop.features.stock.domain.Stock;
import com.os.workshop.features.stock.persistence.adapter.StockPersistenceAdapter;
import com.os.workshop.features.stock.persistence.entity.StockEntity;
import com.os.workshop.features.stock.persistence.mappers.StockMapper;
import com.os.workshop.features.stock.persistence.repository.StockJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPersistenceAdapterTest {

    @Mock
    private StockJpaRepository jpaRepository;

    @Mock
    private StockMapper mapper;

    @InjectMocks
    private StockPersistenceAdapter adapter;

    @Test
    void savesAndQueriesStocks() {
        Stock stock = stock();
        StockEntity entity = new StockEntity();

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(stock);
        when(jpaRepository.findByProductId(2L)).thenReturn(Optional.of(entity));
        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(jpaRepository.existsByProductId(2L)).thenReturn(true);

        assertEquals(1L, adapter.save(stock).getId());
        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByProductId(2L).isPresent());
        assertEquals(1, adapter.findAll().size());
        assertTrue(adapter.existsByProductId(2L));
    }

    private Stock stock() {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProductId(2L);
        stock.setQuantity(BigDecimal.TEN);
        stock.setReservedQuantity(BigDecimal.ONE);
        stock.setMinimumQuantity(BigDecimal.ZERO);
        stock.recalculateAvailableQuantity();
        return stock;
    }
}
