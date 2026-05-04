package com.os.workshop.features.stock.updateMinimum;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateMinimumHandler {

    private final StockRepository stockRepository;

    @Transactional
    public Stock handle(Long productId, BigDecimal minimumQuantity) {
        if (minimumQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum quantity cannot be negative");
        }
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        stock.setMinimumQuantity(minimumQuantity);
        return stockRepository.save(stock);
    }
}
