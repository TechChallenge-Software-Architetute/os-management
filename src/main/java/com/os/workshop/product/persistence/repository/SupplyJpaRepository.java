package com.os.workshop.product.persistence.repository;

import com.os.workshop.product.persistence.entity.SupplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyJpaRepository extends JpaRepository<SupplyEntity, Long> {

    Optional<SupplyEntity> findBySku(String sku);

    List<SupplyEntity> findByActiveTrue();

    List<SupplyEntity> findByCategoryIgnoreCase(String category);

    boolean existsBySku(String sku);
}
