package com.os.workshop.features.stock.findByProductId;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindStockByProductIdHandlerTest {

    @Mock private StockRepository stockRepository;
    @InjectMocks private FindStockByProductIdHandler handler;

    @Test
    void returnsStockWhenFound() {
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        assertEquals(1L, handler.handle(1L).getProductId());
    }

    @Test
    void throwsWhenNotFound() {
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> handler.handle(99L));
    }
}
