package com.os.workshop.features.stock.exit;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockExitHandlerTest {

    @Mock private StockRepository stockRepository;
    @Mock private StockMovementRepository movementRepository;
    @InjectMocks private StockExitHandler handler;

    private Stock createStock() {
        Stock s = new Stock(); s.setId(1L); s.setProductId(1L);
        s.setQuantity(BigDecimal.TEN); s.setReservedQuantity(BigDecimal.ZERO);
        s.setAvailableQuantity(BigDecimal.TEN); s.setMinimumQuantity(BigDecimal.ONE);
        return s;
    }

    @Test
    void removesStockAndRecordsMovement() {
        Stock stock = createStock();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        var result = handler.handle(1L, new StockExitRequest(new BigDecimal("3"), "Sale"));
        assertEquals(new BigDecimal("7"), result.getQuantity());
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void throwsWhenStockNotFound() {
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());
        var result = new StockExitRequest(BigDecimal.ONE, "test");
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(99L, result));
    }
}
