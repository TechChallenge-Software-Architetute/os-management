package com.os.features.product.persistence.repository;

import com.os.features.product.persistence.entity.PartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartJpaRepository extends JpaRepository<PartEntity, UUID> {

    Optional<PartEntity> findBySku(String sku);

    List<PartEntity> findByActiveTrue();

    List<PartEntity> findByCategoryIgnoreCase(String category);

    List<PartEntity> findByBrandIgnoreCase(String brand);

    boolean existsBySku(String sku);
}
