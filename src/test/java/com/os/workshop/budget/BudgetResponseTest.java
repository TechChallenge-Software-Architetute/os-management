package com.os.workshop.budget;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.budget.domain.Budget;
import com.os.workshop.features.budget.domain.BudgetItem;
import com.os.workshop.features.product.domain.ProductType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetResponseTest {

    @Test
    void createsResponseFromBudget() {
        UUID serviceOrderId = UUID.randomUUID();
        BudgetItem item = BudgetItem.snapshot(10L, "Filtro", "FLT-1", ProductType.PART, new BigDecimal("25.50"), new BigDecimal("2"));
        item.setId(3L);
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setServiceOrderId(serviceOrderId);
        budget.setItems(List.of(item));
        budget.recalculateTotalPrice();

        BudgetResponse response = BudgetResponse.from(budget);

        assertEquals(new BigDecimal("51.00"), response.totalPrice());
        assertEquals(serviceOrderId, response.serviceOrderId());
        assertEquals(1, response.items().size());
    }
}
