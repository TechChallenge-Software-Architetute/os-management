package com.os.features.product.repository;

import com.os.features.product.domain.Part;
import com.os.features.product.domain.Supply;

import java.util.Optional;

public interface ProductRepository {

    Part savePart(Part part);

    Supply saveSupply(Supply supply);

    Optional<Part> findPartById(String identifier);

    Optional<Supply> findSupplyById(String identifier);

    void deleteById(String identifier);
}
