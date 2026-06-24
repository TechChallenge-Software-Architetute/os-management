package com.os.workshop.features.product.shared.repository;

import com.os.workshop.features.product.shared.domain.Part;

import java.util.List;
import java.util.Optional;

public interface PartRepository {

    Part save(Part part);

    Optional<Part> findById(Long id);

    List<Part> findAllActive();

    Optional<Part> findBySku(String sku);

    boolean existsBySku(String sku);
}
