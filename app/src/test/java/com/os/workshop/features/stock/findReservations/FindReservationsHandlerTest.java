package com.os.workshop.features.stock.findReservations;

import com.os.workshop.features.stock.shared.domain.StockReservation;
import com.os.workshop.features.stock.shared.repository.StockReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindReservationsHandlerTest {

    @Mock private StockReservationRepository reservationRepository;
    @InjectMocks private FindReservationsHandler handler;

    @Test
    void returnsReservationsForServiceOrder() {
        UUID osId = UUID.randomUUID();
        when(reservationRepository.findByServiceOrderId(osId)).thenReturn(List.of(new StockReservation()));
        assertEquals(1, handler.handle(osId).size());
    }
}
