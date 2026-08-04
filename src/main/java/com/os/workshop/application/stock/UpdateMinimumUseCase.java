package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateMinimumUseCase {

    private final StockRepository stockRepository;

    @Transactional
    public Stock execute(Long productId, BigDecimal minimumQuantity) {
        if (minimumQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum quantity cannot be negative");
        }
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
        stock.setMinimumQuantity(minimumQuantity);
        return stockRepository.save(stock);
    }
}
