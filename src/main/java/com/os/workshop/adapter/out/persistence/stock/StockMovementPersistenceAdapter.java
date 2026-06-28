package com.os.workshop.adapter.out.persistence.stock;

import com.os.workshop.application.stock.port.out.StockMovementRepository;
import com.os.workshop.domain.stock.StockMovement;
import com.os.workshop.infrastructure.persistence.stock.StockMovementJpaRepository;
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
        return stockMovementMapper.toDomain(jpaRepository.save(stockMovementMapper.toEntity(movement)));
    }

    @Override
    public List<StockMovement> findByStockId(Long stockId) {
        return jpaRepository.findByStockIdOrderByCreatedAtDesc(stockId).stream().map(stockMovementMapper::toDomain).toList();
    }
}
