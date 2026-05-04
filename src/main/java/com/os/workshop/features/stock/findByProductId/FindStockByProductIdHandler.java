package com.os.workshop.features.stock.findByProductId;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindStockByProductIdHandler {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public Stock handle(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
    }
}
