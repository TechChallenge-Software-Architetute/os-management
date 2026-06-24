package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.mapper.StockMovementMapper;
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
