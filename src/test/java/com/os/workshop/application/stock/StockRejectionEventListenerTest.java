package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.serviceorder.OrderRejectedEvent;
import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockRejectionEventListenerTest {

    @Mock private ReleaseReservationUseCase releaseReservationUseCase;
    @Mock private StockReservationRepository reservationRepository;
    @InjectMocks private StockRejectionEventListener listener;

    @Test
    void releasesReservationsWhenActiveExist() {
        UUID orderId = UUID.randomUUID();
        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setQuantity(BigDecimal.TEN);
        reservation.setStatus(StockReservationStatus.ACTIVE);

        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));

        listener.onOrderRejected(new OrderRejectedEvent(orderId));

        verify(releaseReservationUseCase).execute(orderId);
    }

    @Test
    void doesNothingWhenNoActiveReservations() {
        UUID orderId = UUID.randomUUID();
        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of());

        listener.onOrderRejected(new OrderRejectedEvent(orderId));

        verify(releaseReservationUseCase, never()).execute(any());
    }
}
