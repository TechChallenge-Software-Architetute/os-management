package com.os.workshop.features.budget.shared.repository;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.domain.product.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetPersistenceAdapterTest {

    @Mock private BudgetJpaRepository budgetJpaRepository;
    @Mock private BudgetItemJpaRepository budgetItemJpaRepository;
    @InjectMocks private BudgetPersistenceAdapter adapter;

    @Test
    void savesNewBudgetWithItems() {
        Budget budget = new Budget();
        budget.setServiceOrderId(UUID.randomUUID());
        budget.setTotalPrice(new BigDecimal("100.00"));
        BudgetItem item = new BudgetItem();
        item.setProductId(1L); item.setProductName("Part"); item.setProductSku("P1");
        item.setProductType(ProductType.PART); item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(new BigDecimal("100")); item.setTotalPrice(new BigDecimal("100"));
        budget.setItems(List.of(item));

        BudgetEntity savedEntity = new BudgetEntity();
        savedEntity.setId(1L);
        savedEntity.setServiceOrderId(budget.getServiceOrderId());
        savedEntity.setTotalPrice(budget.getTotalPrice());

        when(budgetJpaRepository.save(any(BudgetEntity.class))).thenReturn(savedEntity);
        when(budgetItemJpaRepository.findByBudgetId(1L)).thenReturn(List.of());

        Budget result = adapter.save(budget);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(budgetItemJpaRepository).save(any(BudgetItemEntity.class));
    }

    @Test
    void findsByServiceOrderId() {
        UUID osId = UUID.randomUUID();
        BudgetEntity entity = new BudgetEntity();
        entity.setId(1L); entity.setServiceOrderId(osId); entity.setTotalPrice(BigDecimal.TEN);
        when(budgetJpaRepository.findByServiceOrderId(osId)).thenReturn(Optional.of(entity));
        when(budgetItemJpaRepository.findByBudgetId(1L)).thenReturn(List.of());

        assertTrue(adapter.findByServiceOrderId(osId).isPresent());
    }

    @Test
    void deletesItemsByBudgetId() {
        adapter.deleteItemsByBudgetId(1L);
        verify(budgetItemJpaRepository).deleteByBudgetId(1L);
    }
}
