package com.os.features.product.repository;

import com.os.features.product.domain.Part;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartRepository {

    Part save(Part part);

    Optional<Part> findById(UUID id);

    List<Part> findAllActive();

    Optional<Part> findBySku(String sku);

    boolean existsBySku(String sku);
}
