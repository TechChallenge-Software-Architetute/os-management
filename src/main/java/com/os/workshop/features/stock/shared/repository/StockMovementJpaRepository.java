package com.os.workshop.features.stock.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {

    List<StockMovementEntity> findByStockIdOrderByCreatedAtDesc(Long stockId);
}
