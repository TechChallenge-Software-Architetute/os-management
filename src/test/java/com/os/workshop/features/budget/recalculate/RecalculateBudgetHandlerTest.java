package com.os.workshop.features.budget.recalculate;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.repository.BudgetRepository;
import com.os.workshop.features.product.domain.Part;
import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import com.os.workshop.features.product.repository.PartRepository;
import com.os.workshop.features.product.repository.SupplyRepository;
import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import com.os.workshop.features.stock.repository.StockReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecalculateBudgetHandlerTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private SupplyRepository supplyRepository;

    @InjectMocks
    private RecalculateBudgetHandler recalculateBudgetHandler;

    private Part createPart() {
        Part part = new Part();
        part.setId(1L);
        part.setName("Brake Pad");
        part.setSku("BRK-001");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setSalePrice(new BigDecimal("90.00"));
        return part;
    }

    private Supply createSupply() {
        Supply supply = new Supply();
        supply.setId(2L);
        supply.setName("Engine Oil");
        supply.setSku("OIL-5W30");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setSalePrice(new BigDecimal("50.00"));
        return supply;
    }

    private StockReservation createReservation(Long productId, BigDecimal quantity, UUID osId) {
        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setStockId(1L);
        reservation.setProductId(productId);
        reservation.setServiceOrderId(osId);
        reservation.setQuantity(quantity);
        reservation.setStatus(StockReservationStatus.ACTIVE);
        return reservation;
    }

    @Test
    void whenRecalculatingWithActiveReservations_thenBudgetIsCreatedWithCorrectTotal() {
        UUID osId = UUID.randomUUID();
        Part part = createPart();
        Supply supply = createSupply();

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(
                        createReservation(1L, new BigDecimal("2"), osId),
                        createReservation(2L, new BigDecimal("3"), osId)
                ));
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.empty());
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.findById(2L)).thenReturn(Optional.empty());
        when(supplyRepository.findById(2L)).thenReturn(Optional.of(supply));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> {
            Budget b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        Budget result = recalculateBudgetHandler.handle(osId);

        // 2 × 90.00 + 3 × 50.00 = 180.00 + 150.00 = 330.00
        assertEquals(new BigDecimal("330.00"), result.getTotalPrice());
        assertEquals(2, result.getItems().size());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void whenRecalculatingWithNoReservations_thenBudgetHasZeroTotal() {
        UUID osId = UUID.randomUUID();

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of());
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> {
            Budget b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        Budget result = recalculateBudgetHandler.handle(osId);

        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void whenRecalculatingExistingBudget_thenOldItemsAreDeletedAndRebuilt() {
        UUID osId = UUID.randomUUID();
        Part part = createPart();

        Budget existingBudget = new Budget();
        existingBudget.setId(10L);
        existingBudget.setServiceOrderId(osId);

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(createReservation(1L, new BigDecimal("4"), osId)));
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.of(existingBudget));
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> i.getArgument(0));

        Budget result = recalculateBudgetHandler.handle(osId);

        verify(budgetRepository).deleteItemsByBudgetId(10L);
        // 4 × 90.00 = 360.00
        assertEquals(new BigDecimal("360.00"), result.getTotalPrice());
        assertEquals(1, result.getItems().size());
        assertEquals("Brake Pad", result.getItems().get(0).getProductName());
    }

    @Test
    void whenRecalculating_thenItemSnapshotsProductPriceAtCurrentTime() {
        UUID osId = UUID.randomUUID();
        Part part = createPart();
        part.setSalePrice(new BigDecimal("120.00")); // price changed

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(createReservation(1L, new BigDecimal("1"), osId)));
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.empty());
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(i -> {
            Budget b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        Budget result = recalculateBudgetHandler.handle(osId);

        assertEquals(new BigDecimal("120.00"), result.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("120.00"), result.getTotalPrice());
    }
}
