package com.os.workshop.features.stock.list;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListStocksHandler {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<Stock> handle() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Stock> handleLowStock() {
        return stockRepository.findAll().stream()
                .filter(Stock::isLowStock)
                .toList();
    }
}
