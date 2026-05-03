package com.os.workshop.stock.reservation;

import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockReservationResponseTest {

    @Test
    void createsResponseFromReservation() {
        StockReservation reservation = new StockReservation(1L, 1L, 2L, UUID.randomUUID(), BigDecimal.ONE, StockReservationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(StockReservationStatus.ACTIVE, StockReservationResponse.from(reservation).status());
    }
}
