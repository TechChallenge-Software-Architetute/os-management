package com.os.workshop.stock.reservation;

import com.os.workshop.features.stock.reservation.ReservationChangedEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationChangedEventTest {

    @Test
    void exposesServiceOrderId() {
        UUID id = UUID.randomUUID();

        assertEquals(id, new ReservationChangedEvent(id).serviceOrderId());
    }
}
