package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.shared.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockPersistenceAdapter implements StockRepository {

    private final StockJpaRepository jpaRepository;
    private final StockMapper stockMapper;

    @Override
    public Stock save(Stock stock) {
        StockEntity entity;

        if (stock.getId() != null) {
            entity = jpaRepository.findById(stock.getId()).orElse(stockMapper.toEntity(stock));
            stockMapper.updateEntity(entity, stock);
        } else {
            entity = stockMapper.toEntity(stock);
        }

        return stockMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Stock> findById(Long id) {
        return jpaRepository.findById(id).map(stockMapper::toDomain);
    }

    @Override
    public Optional<Stock> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(stockMapper::toDomain);
    }

    @Override
    public List<Stock> findAll() {
        return jpaRepository.findAll().stream()
                .map(stockMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jpaRepository.existsByProductId(productId);
    }
}
