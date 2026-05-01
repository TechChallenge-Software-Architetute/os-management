package com.os.workshop.stock.persistence.repository;

import com.os.workshop.stock.domain.StockReservationStatus;
import com.os.workshop.stock.persistence.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {

    List<StockReservationEntity> findByServiceOrderId(Long serviceOrderId);

    List<StockReservationEntity> findByServiceOrderIdAndStatus(Long serviceOrderId, StockReservationStatus status);

    List<StockReservationEntity> findByStockIdAndStatus(Long stockId, StockReservationStatus status);
}
