package com.os.workshop.features.budget.shared.mapper;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.budget.shared.repository.BudgetEntity;
import com.os.workshop.features.budget.shared.repository.BudgetItemEntity;
import com.os.workshop.domain.product.ProductType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetMapperTest {

    @Test
    void mapsBudgetEntityToDomain() {
        BudgetEntity entity = new BudgetEntity();
        entity.setId(1L);
        entity.setServiceOrderId(UUID.randomUUID());
        entity.setTotalPrice(new BigDecimal("100.00"));

        Budget budget = BudgetMapper.toDomain(entity);

        assertEquals(1L, budget.getId());
        assertEquals(entity.getServiceOrderId(), budget.getServiceOrderId());
        assertEquals(new BigDecimal("100.00"), budget.getTotalPrice());
    }

    @Test
    void mapsBudgetItemEntityToDomain() {
        BudgetItemEntity entity = new BudgetItemEntity();
        entity.setId(1L);
        entity.setBudgetId(1L);
        entity.setProductId(10L);
        entity.setProductName("Brake Pad");
        entity.setProductSku("BP-001");
        entity.setProductType(ProductType.PART);
        entity.setQuantity(new BigDecimal("2"));
        entity.setUnitPrice(new BigDecimal("50.00"));
        entity.setTotalPrice(new BigDecimal("100.00"));

        BudgetItem item = BudgetMapper.itemToDomain(entity);

        assertEquals(1L, item.getId());
        assertEquals("Brake Pad", item.getProductName());
        assertEquals(ProductType.PART, item.getProductType());
        assertEquals(new BigDecimal("100.00"), item.getTotalPrice());
    }
}
