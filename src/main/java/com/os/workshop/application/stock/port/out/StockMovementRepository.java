package com.os.workshop.application.stock.port.out;

import com.os.workshop.domain.stock.StockMovement;

import java.util.List;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByStockId(Long stockId);
}
