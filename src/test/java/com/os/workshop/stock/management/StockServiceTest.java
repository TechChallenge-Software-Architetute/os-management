package com.os.workshop.stock.management;

import com.os.workshop.stock.domain.Stock;
import com.os.workshop.stock.domain.StockMovement;
import com.os.workshop.stock.repository.StockMovementRepository;
import com.os.workshop.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private StockService stockService;

    private Stock createStock() {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProductId(2L);
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(new BigDecimal("10"));
        stock.recalculateAvailableQuantity();
        return stock;
    }

    @Test
    void whenCreatingStockForNewProduct_thenStockIsSaved() {
        Long productId = 3L;
        StockRequest request = new StockRequest(productId, new BigDecimal("50"), new BigDecimal("5"));
        when(stockRepository.existsByProductId(productId)).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        Stock result = stockService.create(request);

        assertEquals(new BigDecimal("50"), result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void whenCreatingStockForExistingProduct_thenThrowsIllegalArgument() {
        Long productId = 4L;
        StockRequest request = new StockRequest(productId, new BigDecimal("50"), new BigDecimal("5"));
        when(stockRepository.existsByProductId(productId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> stockService.create(request));
    }

    @Test
    void whenAddingStock_thenQuantityIncreasesAndMovementIsRecorded() {
        Stock stock = createStock();
        when(stockRepository.findByProductId(stock.getProductId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementRequest request = new StockMovementRequest(new BigDecimal("25"), "Delivery");
        Stock result = stockService.addStock(stock.getProductId(), request);

        assertEquals(new BigDecimal("125"), result.getQuantity());
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void whenRemovingStock_thenQuantityDecreasesAndMovementIsRecorded() {
        Stock stock = createStock();
        when(stockRepository.findByProductId(stock.getProductId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementRequest request = new StockMovementRequest(new BigDecimal("20"), "Used");
        Stock result = stockService.removeStock(stock.getProductId(), request);

        assertEquals(new BigDecimal("80"), result.getQuantity());
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void whenFindingLowStock_thenReturnsOnlyItemsBelowMinimum() {
        Stock low = createStock();
        low.setQuantity(new BigDecimal("5"));
        low.recalculateAvailableQuantity();

        Stock ok = createStock();

        when(stockRepository.findAll()).thenReturn(List.of(low, ok));

        List<Stock> result = stockService.findLowStock();

        assertEquals(1, result.size());
    }

    @Test
    void whenUpdatingMinimumWithNegativeValue_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> stockService.updateMinimumQuantity(99L, new BigDecimal("-1")));
    }
}
