package com.os.workshop.features.stock.persistence.adapter;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import com.os.workshop.features.stock.persistence.entity.StockReservationEntity;
import com.os.workshop.features.stock.persistence.mappers.StockReservationMapper;
import com.os.workshop.features.stock.persistence.repository.StockReservationJpaRepository;
import com.os.workshop.features.stock.repository.StockReservationRepository;
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
            entity = jpaRepository.findById(reservation.getId())
                    .orElse(stockReservationMapper.toEntity(reservation));
            stockReservationMapper.updateEntity(entity, reservation);
        } else {
            entity = stockReservationMapper.toEntity(reservation);
        }

        return stockReservationMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<StockReservation> findById(Long id) {
        return jpaRepository.findById(id).map(stockReservationMapper::toDomain);
    }

    @Override
    public List<StockReservation> findByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(stockReservationMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockReservation> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status) {
        return jpaRepository.findByServiceOrderIdAndStatus(serviceOrderId, status).stream()
                .map(stockReservationMapper::toDomain)
                .toList();
    }
}
