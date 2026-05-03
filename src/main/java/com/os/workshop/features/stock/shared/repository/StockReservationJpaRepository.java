package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {

    List<StockReservationEntity> findByServiceOrderId(UUID serviceOrderId);

    List<StockReservationEntity> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status);

    List<StockReservationEntity> findByStockIdAndStatus(Long stockId, StockReservationStatus status);
}
