package com.os.workshop.stock.repository;

import com.os.workshop.stock.domain.StockMovement;

import java.util.List;

public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    List<StockMovement> findByStockId(Long stockId);
}
