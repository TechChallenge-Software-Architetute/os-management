package com.os.workshop.stock.persistence.repository;

import com.os.workshop.stock.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {

    List<StockMovementEntity> findByStockIdOrderByCreatedAtDesc(Long stockId);
}
