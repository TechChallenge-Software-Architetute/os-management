package com.os.workshop.infrastructure.persistence.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {
    Optional<StockEntity> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
}
