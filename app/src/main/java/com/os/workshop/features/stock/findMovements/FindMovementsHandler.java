package com.os.workshop.features.stock.findMovements;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindMovementsHandler {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    @Transactional(readOnly = true)
    public List<StockMovement> handle(Long productId) {
        var stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        return movementRepository.findByStockId(stock.getId());
    }
}
