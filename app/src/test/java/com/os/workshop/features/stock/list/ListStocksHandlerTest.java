package com.os.workshop.features.stock.list;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStocksHandlerTest {

    @Mock private StockRepository stockRepository;
    @InjectMocks private ListStocksHandler handler;

    @Test
    void returnsAllStocks() {
        when(stockRepository.findAll()).thenReturn(List.of(new Stock()));
        assertEquals(1, handler.handle().size());
    }

    @Test
    void returnsOnlyLowStock() {
        Stock low = new Stock(); low.setQuantity(BigDecimal.ONE); low.setReservedQuantity(BigDecimal.ZERO);
        low.setAvailableQuantity(BigDecimal.ONE); low.setMinimumQuantity(BigDecimal.TEN);
        Stock ok = new Stock(); ok.setQuantity(BigDecimal.TEN); ok.setReservedQuantity(BigDecimal.ZERO);
        ok.setAvailableQuantity(BigDecimal.TEN); ok.setMinimumQuantity(BigDecimal.ONE);
        when(stockRepository.findAll()).thenReturn(List.of(low, ok));
        assertEquals(1, handler.handleLowStock().size());
    }
}
