package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockMovementRepository;
import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.domain.stock.Stock;
import com.os.workshop.domain.stock.StockMovement;
import com.os.workshop.domain.stock.StockMovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StockEntryUseCase {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    @Transactional
    public Stock execute(Long productId, BigDecimal quantity, String reason) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        stock.addQuantity(quantity);
        Stock saved = stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setStockId(saved.getId());
        movement.setType(StockMovementType.ENTRY);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movementRepository.save(movement);

        return saved;
    }
}
