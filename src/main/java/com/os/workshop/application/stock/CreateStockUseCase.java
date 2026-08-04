package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateStockUseCase {

    private final StockRepository stockRepository;

    @Transactional
    public Stock execute(Long productId, BigDecimal quantity, BigDecimal minimumQuantity) {
        if (stockRepository.existsByProductId(productId)) {
            throw new IllegalArgumentException("Stock already exists for product: " + productId);
        }
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setQuantity(quantity);
        stock.setMinimumQuantity(minimumQuantity);
        return stockRepository.save(stock);
    }
}
