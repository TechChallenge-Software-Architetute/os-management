package com.os.workshop.features.stock.entry;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.domain.StockMovementType;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockEntryHandler {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    @Transactional
    public Stock handle(Long productId, StockEntryRequest request) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        stock.addQuantity(request.quantity());

        Stock saved = stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setStockId(saved.getId());
        movement.setType(StockMovementType.ENTRY);
        movement.setQuantity(request.quantity());
        movement.setReason(request.reason());
        movementRepository.save(movement);

        return saved;
    }
}
