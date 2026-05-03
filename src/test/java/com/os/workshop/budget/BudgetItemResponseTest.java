package com.os.workshop.budget;

import com.os.workshop.features.budget.BudgetItemResponse;
import com.os.workshop.features.budget.domain.BudgetItem;
import com.os.workshop.features.product.domain.ProductType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetItemResponseTest {

    @Test
    void createsResponseFromBudgetItem() {
        BudgetItem item = BudgetItem.snapshot(10L, "Filtro", "FLT-1", ProductType.PART, new BigDecimal("25.50"), new BigDecimal("2"));
        item.setId(3L);

        BudgetItemResponse response = BudgetItemResponse.from(item);

        assertEquals(3L, response.id());
        assertEquals(ProductType.PART, response.productType());
        assertEquals(new BigDecimal("51.00"), response.totalPrice());
    }
}
