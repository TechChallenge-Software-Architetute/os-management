package com.os.workshop.product.repository;

import com.os.workshop.product.domain.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository {

    Supply save(Supply supply);

    Optional<Supply> findById(Long id);

    List<Supply> findAllActive();

    Optional<Supply> findBySku(String sku);

    boolean existsBySku(String sku);
}
