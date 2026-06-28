package com.os.workshop.infrastructure.persistence.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {
    List<StockMovementEntity> findByStockIdOrderByCreatedAtDesc(Long stockId);
}
