package com.os.workshop.features.stock.create;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateStockHandlerTest {

    @Mock private StockRepository stockRepository;
    @InjectMocks private CreateStockHandler handler;

    @Test
    void createsStockSuccessfully() {
        when(stockRepository.existsByProductId(1L)).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> {
            Stock s = i.getArgument(0); s.setId(1L); return s;
        });
        var result = handler.handle(new CreateStockRequest(1L, BigDecimal.TEN, BigDecimal.ONE));
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void throwsWhenStockAlreadyExists() {
        when(stockRepository.existsByProductId(1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateStockRequest(1L, BigDecimal.TEN, BigDecimal.ONE)));
        verify(stockRepository, never()).save(any());
    }
}
