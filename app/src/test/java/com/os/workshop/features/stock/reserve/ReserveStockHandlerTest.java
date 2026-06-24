package com.os.workshop.features.stock.reserve;

import com.os.workshop.features.stock.shared.domain.ReservationChangedEvent;
import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.domain.StockReservation;
import com.os.workshop.features.stock.shared.domain.StockReservationStatus;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import com.os.workshop.features.stock.shared.repository.StockReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveStockHandlerTest {

    @Mock private StockRepository stockRepository;
    @Mock private StockReservationRepository reservationRepository;
    @Mock private StockMovementRepository movementRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ReserveStockHandler handler;

    @Test
    void reservesStockSuccessfully() {
        UUID osId = UUID.randomUUID();
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        stock.setQuantity(BigDecimal.TEN); stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(BigDecimal.TEN);

        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> {
            StockReservation r = i.getArgument(0); r.setId(1L); return r;
        });

        var request = new ReserveStockRequest(osId, List.of(new ReserveStockItemRequest(1L, new BigDecimal("5"))));
        var result = handler.handle(request);

        assertEquals(1, result.size());
        assertEquals(StockReservationStatus.ACTIVE, result.get(0).getStatus());
        verify(eventPublisher).publishEvent(any(ReservationChangedEvent.class));
    }

    @Test
    void throwsWhenInsufficientStock() {
        UUID osId = UUID.randomUUID();
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        stock.setQuantity(BigDecimal.ONE); stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(BigDecimal.ONE);

        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        var request = new ReserveStockRequest(osId, List.of(new ReserveStockItemRequest(1L, BigDecimal.TEN)));
        assertThrows(IllegalStateException.class, () -> handler.handle(request));
    }

    @Test
    void throwsWhenProductStockNotFound() {
        UUID osId = UUID.randomUUID();
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());
        var request = new ReserveStockRequest(osId, List.of(new ReserveStockItemRequest(99L, BigDecimal.ONE)));
        assertThrows(IllegalArgumentException.class, () -> handler.handle(request));
    }
}
