package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListStocksUseCase {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<Stock> execute() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Stock> executeLowStock() {
        return stockRepository.findAll().stream().filter(Stock::isLowStock).toList();
    }
}
