package com.os.workshop.features.budget.shared.mapper;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.budget.shared.repository.BudgetEntity;
import com.os.workshop.features.budget.shared.repository.BudgetItemEntity;

/**
 * Maps between persistence entities and domain objects for the budget module.
 */
public class BudgetMapper {

    private BudgetMapper() {
    }

    public static Budget toDomain(BudgetEntity entity) {
        Budget budget = new Budget();
        budget.setId(entity.getId());
        budget.setServiceOrderId(entity.getServiceOrderId());
        budget.setTotalPrice(entity.getTotalPrice());
        budget.setCreatedAt(entity.getCreatedAt());
        budget.setUpdatedAt(entity.getUpdatedAt());
        return budget;
    }

    public static BudgetItem itemToDomain(BudgetItemEntity entity) {
        BudgetItem item = new BudgetItem();
        item.setId(entity.getId());
        item.setBudgetId(entity.getBudgetId());
        item.setProductId(entity.getProductId());
        item.setProductName(entity.getProductName());
        item.setProductSku(entity.getProductSku());
        item.setProductType(entity.getProductType());
        item.setQuantity(entity.getQuantity());
        item.setUnitPrice(entity.getUnitPrice());
        item.setTotalPrice(entity.getTotalPrice());
        return item;
    }
}
