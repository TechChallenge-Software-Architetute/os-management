package com.os.workshop.features.stock.persistence.mappers;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockReservationMapperTest {

    private final StockReservationMapper mapper = new StockReservationMapperImpl();

    @Test
    void convertsAndUpdatesStatus() {
        StockReservation reservation = new StockReservation(1L, 1L, 2L, UUID.randomUUID(), BigDecimal.ONE, StockReservationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        var entity = mapper.toEntity(reservation);

        reservation.setStatus(StockReservationStatus.RELEASED);
        mapper.updateEntity(entity, reservation);

        assertEquals(StockReservationStatus.RELEASED, mapper.toDomain(entity).getStatus());
    }
}
