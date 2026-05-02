package com.os.workshop.budget.persistence.adapter;

import com.os.workshop.budget.domain.Budget;
import com.os.workshop.budget.domain.BudgetItem;
import com.os.workshop.budget.persistence.entity.BudgetEntity;
import com.os.workshop.budget.persistence.entity.BudgetItemEntity;
import com.os.workshop.budget.persistence.repository.BudgetItemJpaRepository;
import com.os.workshop.budget.persistence.repository.BudgetJpaRepository;
import com.os.workshop.budget.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetPersistenceAdapter implements BudgetRepository {

    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetItemJpaRepository budgetItemJpaRepository;

    @Override
    public Budget save(Budget budget) {
        BudgetEntity entity;

        if (budget.getId() != null) {
            entity = budgetJpaRepository.findById(budget.getId())
                    .orElseGet(BudgetEntity::new);
        } else {
            entity = new BudgetEntity();
        }

        entity.setServiceOrderId(budget.getServiceOrderId());
        entity.setTotalPrice(budget.getTotalPrice());
        BudgetEntity savedBudget = budgetJpaRepository.save(entity);

        // Save items
        for (BudgetItem item : budget.getItems()) {
            BudgetItemEntity itemEntity = new BudgetItemEntity();
            itemEntity.setBudgetId(savedBudget.getId());
            itemEntity.setProductId(item.getProductId());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setProductSku(item.getProductSku());
            itemEntity.setProductType(item.getProductType());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            itemEntity.setTotalPrice(item.getTotalPrice());
            budgetItemJpaRepository.save(itemEntity);
        }

        return toDomain(savedBudget);
    }

    @Override
    public Optional<Budget> findByServiceOrderId(UUID serviceOrderId) {
        return budgetJpaRepository.findByServiceOrderId(serviceOrderId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteItemsByBudgetId(Long budgetId) {
        budgetItemJpaRepository.deleteByBudgetId(budgetId);
    }

    private Budget toDomain(BudgetEntity entity) {
        Budget budget = new Budget();
        budget.setId(entity.getId());
        budget.setServiceOrderId(entity.getServiceOrderId());
        budget.setTotalPrice(entity.getTotalPrice());
        budget.setCreatedAt(entity.getCreatedAt());
        budget.setUpdatedAt(entity.getUpdatedAt());

        List<BudgetItem> items = budgetItemJpaRepository.findByBudgetId(entity.getId()).stream()
                .map(this::itemToDomain)
                .toList();
        budget.setItems(new java.util.ArrayList<>(items));

        return budget;
    }

    private BudgetItem itemToDomain(BudgetItemEntity entity) {
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
