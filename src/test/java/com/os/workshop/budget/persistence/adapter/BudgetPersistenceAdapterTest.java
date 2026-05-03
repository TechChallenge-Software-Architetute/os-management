package com.os.workshop.budget.persistence.adapter;

import com.os.workshop.budget.domain.Budget;
import com.os.workshop.budget.domain.BudgetItem;
import com.os.workshop.budget.persistence.entity.BudgetEntity;
import com.os.workshop.budget.persistence.entity.BudgetItemEntity;
import com.os.workshop.budget.persistence.repository.BudgetItemJpaRepository;
import com.os.workshop.budget.persistence.repository.BudgetJpaRepository;
import com.os.workshop.product.domain.ProductType;
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
class BudgetPersistenceAdapterTest {

    @Mock
    private BudgetJpaRepository budgetJpaRepository;

    @Mock
    private BudgetItemJpaRepository budgetItemJpaRepository;

    @InjectMocks
    private BudgetPersistenceAdapter adapter;

    @Test
    void savesBudgetAndItems() {
        UUID serviceOrderId = UUID.randomUUID();
        Budget budget = new Budget();
        budget.setServiceOrderId(serviceOrderId);
        budget.setItems(List.of(BudgetItem.snapshot(1L, "Filtro", "F1", ProductType.PART, BigDecimal.TEN, new BigDecimal("2"))));
        budget.recalculateTotalPrice();
        BudgetEntity saved = budgetEntity(1L, serviceOrderId);
        BudgetItemEntity item = budgetItemEntity();

        when(budgetJpaRepository.save(any(BudgetEntity.class))).thenReturn(saved);
        when(budgetItemJpaRepository.findByBudgetId(1L)).thenReturn(List.of(item));

        Budget result = adapter.save(budget);

        assertEquals(serviceOrderId, result.getServiceOrderId());
        assertEquals(1, result.getItems().size());
        verify(budgetItemJpaRepository).save(any(BudgetItemEntity.class));
    }

    @Test
    void findsByServiceOrderIdAndDeletesItems() {
        UUID serviceOrderId = UUID.randomUUID();
        BudgetEntity entity = budgetEntity(1L, serviceOrderId);

        when(budgetJpaRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.of(entity));
        when(budgetItemJpaRepository.findByBudgetId(1L)).thenReturn(List.of());

        assertTrue(adapter.findByServiceOrderId(serviceOrderId).isPresent());

        adapter.deleteItemsByBudgetId(1L);

        verify(budgetItemJpaRepository).deleteByBudgetId(1L);
    }

    private BudgetEntity budgetEntity(Long id, UUID serviceOrderId) {
        BudgetEntity entity = new BudgetEntity();
        entity.setId(id);
        entity.setServiceOrderId(serviceOrderId);
        entity.setTotalPrice(new BigDecimal("20"));
        return entity;
    }

    private BudgetItemEntity budgetItemEntity() {
        BudgetItemEntity entity = new BudgetItemEntity();
        entity.setId(2L);
        entity.setBudgetId(1L);
        entity.setProductId(1L);
        entity.setProductName("Filtro");
        entity.setProductSku("F1");
        entity.setProductType(ProductType.PART);
        entity.setQuantity(new BigDecimal("2"));
        entity.setUnitPrice(BigDecimal.TEN);
        entity.setTotalPrice(new BigDecimal("20"));
        return entity;
    }
}
