package com.os.workshop.features.stock.persistence.repository;

import com.os.workshop.features.stock.persistence.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
