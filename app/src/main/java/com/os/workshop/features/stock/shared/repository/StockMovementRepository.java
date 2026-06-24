package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockMovement;

import java.util.List;

public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    List<StockMovement> findByStockId(Long stockId);
}
