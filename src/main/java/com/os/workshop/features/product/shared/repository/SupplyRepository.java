package com.os.workshop.features.product.shared.repository;

import com.os.workshop.features.product.shared.domain.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository {

    Supply save(Supply supply);

    Optional<Supply> findById(Long id);

    List<Supply> findAllActive();

    Optional<Supply> findBySku(String sku);

    boolean existsBySku(String sku);
}
