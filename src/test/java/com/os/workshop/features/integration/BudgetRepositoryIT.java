package com.os.workshop.features.integration;

import com.os.workshop.features.budget.shared.repository.BudgetEntity;
import com.os.workshop.features.budget.shared.repository.BudgetItemEntity;
import com.os.workshop.features.budget.shared.repository.BudgetItemJpaRepository;
import com.os.workshop.features.budget.shared.repository.BudgetJpaRepository;
import com.os.workshop.domain.product.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates Budget and BudgetItem entity mappings, including the relationship
 * between budget and its items via budgetId.
 */
class BudgetRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private BudgetJpaRepository budgetJpaRepository;

    @Autowired
    private BudgetItemJpaRepository budgetItemJpaRepository;

    @Test
    void savesAndFindsBudgetByServiceOrderId() {
        UUID osId = UUID.randomUUID();

        BudgetEntity budget = new BudgetEntity();
        budget.setServiceOrderId(osId);
        budget.setTotalPrice(new BigDecimal("250.00"));

        BudgetEntity saved = budgetJpaRepository.save(budget);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        var found = budgetJpaRepository.findByServiceOrderId(osId);
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("250.00"), found.get().getTotalPrice());
    }

    @Test
    void savesBudgetItemsAndFindsByBudgetId() {
        UUID osId = UUID.randomUUID();

        BudgetEntity budget = new BudgetEntity();
        budget.setServiceOrderId(osId);
        budget.setTotalPrice(new BigDecimal("190.00"));
        BudgetEntity savedBudget = budgetJpaRepository.save(budget);

        BudgetItemEntity item = new BudgetItemEntity();
        item.setBudgetId(savedBudget.getId());
        item.setProductId(1L);
        item.setProductName("Brake Pad");
        item.setProductSku("BP-001");
        item.setProductType(ProductType.PART);
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("45.00"));
        item.setTotalPrice(new BigDecimal("90.00"));

        BudgetItemEntity item2 = new BudgetItemEntity();
        item2.setBudgetId(savedBudget.getId());
        item2.setProductId(2L);
        item2.setProductName("Engine Oil");
        item2.setProductSku("OIL-001");
        item2.setProductType(ProductType.SUPPLY);
        item2.setQuantity(new BigDecimal("4"));
        item2.setUnitPrice(new BigDecimal("25.00"));
        item2.setTotalPrice(new BigDecimal("100.00"));

        budgetItemJpaRepository.save(item);
        budgetItemJpaRepository.save(item2);

        var items = budgetItemJpaRepository.findByBudgetId(savedBudget.getId());
        assertEquals(2, items.size());
    }

    @Test
    @Transactional
    void deletesBudgetItemsByBudgetId() {
        UUID osId = UUID.randomUUID();

        BudgetEntity budget = new BudgetEntity();
        budget.setServiceOrderId(osId);
        budget.setTotalPrice(BigDecimal.TEN);
        BudgetEntity savedBudget = budgetJpaRepository.save(budget);

        BudgetItemEntity item = new BudgetItemEntity();
        item.setBudgetId(savedBudget.getId());
        item.setProductId(1L);
        item.setProductName("Part");
        item.setProductSku("P1");
        item.setProductType(ProductType.PART);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(BigDecimal.TEN);
        item.setTotalPrice(BigDecimal.TEN);
        budgetItemJpaRepository.save(item);

        budgetItemJpaRepository.deleteByBudgetId(savedBudget.getId());

        assertTrue(budgetItemJpaRepository.findByBudgetId(savedBudget.getId()).isEmpty());
    }
}
