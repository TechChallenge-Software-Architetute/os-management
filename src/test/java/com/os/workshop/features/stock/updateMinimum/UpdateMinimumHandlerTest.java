package com.os.workshop.features.stock.updateMinimum;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMinimumHandlerTest {

    @Mock private StockRepository stockRepository;
    @InjectMocks private UpdateMinimumHandler handler;

    @Test
    void updatesMinimumQuantity() {
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        var result = handler.handle(1L, new BigDecimal("5"));
        assertEquals(new BigDecimal("5"), result.getMinimumQuantity());
    }

    @Test
    void throwsWhenNegativeMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(1L, new BigDecimal("-1")));
    }

    @Test
    void throwsWhenStockNotFound() {
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(99L, BigDecimal.ONE));
    }
}
