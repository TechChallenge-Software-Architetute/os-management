package com.os.workshop.features.integration;

import com.os.workshop.features.stock.shared.domain.StockMovementType;
import com.os.workshop.features.stock.shared.domain.StockReservationStatus;
import com.os.workshop.features.stock.shared.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates Stock, StockMovement, and StockReservation entity mappings,
 * including enum persistence (EnumType.STRING) and sequence generators.
 */
class StockRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private StockMovementJpaRepository stockMovementJpaRepository;

    @Autowired
    private StockReservationJpaRepository stockReservationJpaRepository;

    @Test
    void savesAndFindsStockByProductId() {
        StockEntity stock = new StockEntity();
        stock.setProductId(1001L);
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(new BigDecimal("10"));

        StockEntity saved = stockJpaRepository.save(stock);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        var found = stockJpaRepository.findByProductId(1001L);
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("100"), found.get().getQuantity());
        assertTrue(stockJpaRepository.existsByProductId(1001L));
    }

    @Test
    void savesStockMovementWithEnumType() {
        StockEntity stock = new StockEntity();
        stock.setProductId(1002L);
        stock.setQuantity(BigDecimal.TEN);
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(BigDecimal.ONE);
        StockEntity savedStock = stockJpaRepository.save(stock);

        StockMovementEntity movement = new StockMovementEntity();
        movement.setStockId(savedStock.getId());
        movement.setType(StockMovementType.ENTRY);
        movement.setQuantity(new BigDecimal("5"));
        movement.setReason("Initial stock");

        StockMovementEntity saved = stockMovementJpaRepository.save(movement);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        var found = stockMovementJpaRepository.findByStockIdOrderByCreatedAtDesc(savedStock.getId());
        assertEquals(1, found.size());
        assertEquals(StockMovementType.ENTRY, found.get(0).getType());
    }

    @Test
    void savesStockReservationWithStatusEnum() {
        StockEntity stock = new StockEntity();
        stock.setProductId(1003L);
        stock.setQuantity(new BigDecimal("50"));
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(BigDecimal.ONE);
        StockEntity savedStock = stockJpaRepository.save(stock);

        UUID osId = UUID.randomUUID();

        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setStockId(savedStock.getId());
        reservation.setProductId(1003L);
        reservation.setServiceOrderId(osId);
        reservation.setQuantity(new BigDecimal("10"));
        reservation.setStatus(StockReservationStatus.ACTIVE);

        StockReservationEntity saved = stockReservationJpaRepository.save(reservation);

        assertNotNull(saved.getId());

        var found = stockReservationJpaRepository.findByServiceOrderId(osId);
        assertEquals(1, found.size());
        assertEquals(StockReservationStatus.ACTIVE, found.get(0).getStatus());

        var active = stockReservationJpaRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE);
        assertEquals(1, active.size());

        var confirmed = stockReservationJpaRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.CONFIRMED);
        assertTrue(confirmed.isEmpty());
    }
}
