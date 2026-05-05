package com.os.workshop.features.stock.findMovements;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindMovementsHandlerTest {

    @Mock private StockRepository stockRepository;
    @Mock private StockMovementRepository movementRepository;
    @InjectMocks private FindMovementsHandler handler;

    @Test
    void returnsMovementsForProduct() {
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(movementRepository.findByStockId(1L)).thenReturn(List.of(new StockMovement()));
        assertEquals(1, handler.handle(1L).size());
    }

    @Test
    void throwsWhenStockNotFound() {
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> handler.handle(99L));
    }
}
