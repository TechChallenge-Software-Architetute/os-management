package com.os.workshop.features.stock.create;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateStockHandler {

    private final StockRepository stockRepository;

    @Transactional
    public Stock handle(CreateStockRequest request) {
        if (stockRepository.existsByProductId(request.productId())) {
            throw new IllegalArgumentException("Stock already exists for product: " + request.productId());
        }

        Stock stock = new Stock();
        stock.setProductId(request.productId());
        stock.setQuantity(request.quantity());
        stock.setMinimumQuantity(request.minimumQuantity());
        return stockRepository.save(stock);
    }
}
