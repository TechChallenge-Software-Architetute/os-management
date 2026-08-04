package com.os.workshop.adapter.out.persistence.stock;

import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;
import com.os.workshop.infrastructure.persistence.stock.StockReservationEntity;
import com.os.workshop.infrastructure.persistence.stock.StockReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockReservationPersistenceAdapter implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;
    private final StockReservationMapper stockReservationMapper;

    @Override
    public StockReservation save(StockReservation reservation) {
        StockReservationEntity entity;
        if (reservation.getId() != null) {
            entity = jpaRepository.findById(reservation.getId()).orElse(stockReservationMapper.toEntity(reservation));
            stockReservationMapper.updateEntity(entity, reservation);
        } else {
            entity = stockReservationMapper.toEntity(reservation);
        }
        return stockReservationMapper.toDomain(jpaRepository.save(entity));
    }

    @Override public Optional<StockReservation> findById(Long id) { return jpaRepository.findById(id).map(stockReservationMapper::toDomain); }
    @Override public List<StockReservation> findByServiceOrderId(UUID serviceOrderId) { return jpaRepository.findByServiceOrderId(serviceOrderId).stream().map(stockReservationMapper::toDomain).toList(); }
    @Override public List<StockReservation> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status) { return jpaRepository.findByServiceOrderIdAndStatus(serviceOrderId, status).stream().map(stockReservationMapper::toDomain).toList(); }
}
