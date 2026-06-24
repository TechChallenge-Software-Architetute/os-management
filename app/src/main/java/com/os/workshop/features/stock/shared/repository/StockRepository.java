package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Stock save(Stock stock);

    Optional<Stock> findById(Long id);

    Optional<Stock> findByProductId(Long productId);

    List<Stock> findAll();

    boolean existsByProductId(Long productId);
}
