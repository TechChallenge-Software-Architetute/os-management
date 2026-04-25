package com.os.features.product.persistence.repository;

import com.os.features.product.persistence.entity.SupplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplyJpaRepository extends JpaRepository<SupplyEntity, UUID> {

    Optional<SupplyEntity> findBySku(String sku);

    List<SupplyEntity> findByActiveTrue();

    List<SupplyEntity> findByCategoryIgnoreCase(String category);

    boolean existsBySku(String sku);
}
