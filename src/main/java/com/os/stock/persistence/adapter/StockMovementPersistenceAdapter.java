package com.os.workshop.stock.persistence.adapter;

import com.os.workshop.stock.domain.StockMovement;
import com.os.workshop.stock.persistence.mappers.StockMovementMapper;
import com.os.workshop.stock.persistence.repository.StockMovementJpaRepository;
import com.os.workshop.stock.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockMovementPersistenceAdapter implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;
    private final StockMovementMapper stockMovementMapper;

    @Override
    public StockMovement save(StockMovement movement) {
        var entity = stockMovementMapper.toEntity(movement);
        return stockMovementMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<StockMovement> findByStockId(Long stockId) {
        return jpaRepository.findByStockIdOrderByCreatedAtDesc(stockId).stream()
                .map(stockMovementMapper::toDomain)
                .toList();
    }
}
