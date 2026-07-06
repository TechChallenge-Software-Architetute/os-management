package com.os.workshop.infrastructure.persistence.stock;

import com.os.workshop.domain.stock.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {
    List<StockReservationEntity> findByServiceOrderId(UUID serviceOrderId);
    List<StockReservationEntity> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status);
}
