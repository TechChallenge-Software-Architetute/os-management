package com.os.workshop.application.product.supply.port.out;

import com.os.workshop.domain.product.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository {

    Supply save(Supply supply);

    Optional<Supply> findById(Long id);

    List<Supply> findAllActive();

    Optional<Supply> findBySku(String sku);

    boolean existsBySku(String sku);
}
