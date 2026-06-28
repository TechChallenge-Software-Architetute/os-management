package com.os.workshop.application.stock.port.out;

import com.os.workshop.domain.stock.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findById(Long id);
    Optional<Stock> findByProductId(Long productId);
    List<Stock> findAll();
    boolean existsByProductId(Long productId);
}
