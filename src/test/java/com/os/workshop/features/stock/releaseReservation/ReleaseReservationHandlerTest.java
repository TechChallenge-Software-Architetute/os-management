package com.os.workshop.features.stock.releaseReservation;

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
class ReleaseReservationHandlerTest {

    @Mock private StockRepository stockRepository;
    @Mock private StockReservationRepository reservationRepository;
    @Mock private StockMovementRepository movementRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ReleaseReservationHandler handler;

    @Test
    void releasesActiveReservations() {
        UUID osId = UUID.randomUUID();
        StockReservation reservation = new StockReservation();
        reservation.setId(1L); reservation.setStockId(1L); reservation.setQuantity(BigDecimal.TEN);
        reservation.setStatus(StockReservationStatus.ACTIVE);

        Stock stock = new Stock(); stock.setId(1L); stock.setQuantity(new BigDecimal("20"));
        stock.setReservedQuantity(BigDecimal.TEN); stock.setAvailableQuantity(BigDecimal.TEN);

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        var result = handler.handle(osId);

        assertEquals(1, result.size());
        assertEquals(StockReservationStatus.RELEASED, result.get(0).getStatus());
        verify(eventPublisher).publishEvent(any(ReservationChangedEvent.class));
    }

    @Test
    void throwsWhenNoActiveReservations() {
        UUID osId = UUID.randomUUID();
        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> handler.handle(osId));
    }
}
