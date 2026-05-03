package com.os.workshop.features.stock.persistence.adapter;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import com.os.workshop.features.stock.persistence.adapter.StockReservationPersistenceAdapter;
import com.os.workshop.features.stock.persistence.entity.StockReservationEntity;
import com.os.workshop.features.stock.persistence.mappers.StockReservationMapper;
import com.os.workshop.features.stock.persistence.repository.StockReservationJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReservationPersistenceAdapterTest {

    @Mock
    private StockReservationJpaRepository jpaRepository;

    @Mock
    private StockReservationMapper mapper;

    @InjectMocks
    private StockReservationPersistenceAdapter adapter;

    @Test
    void savesAndQueriesReservations() {
        StockReservation reservation = new StockReservation(1L, 1L, 2L, UUID.randomUUID(), BigDecimal.ONE, StockReservationStatus.ACTIVE, null, null);
        StockReservationEntity entity = new StockReservationEntity();

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(reservation);
        when(jpaRepository.findByServiceOrderId(reservation.getServiceOrderId())).thenReturn(List.of(entity));
        when(jpaRepository.findByServiceOrderIdAndStatus(reservation.getServiceOrderId(), StockReservationStatus.ACTIVE)).thenReturn(List.of(entity));

        assertEquals(1L, adapter.save(reservation).getId());
        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(1, adapter.findByServiceOrderId(reservation.getServiceOrderId()).size());
        assertEquals(1, adapter.findByServiceOrderIdAndStatus(reservation.getServiceOrderId(), StockReservationStatus.ACTIVE).size());
    }
}
