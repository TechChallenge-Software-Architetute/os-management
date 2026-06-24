package com.os.workshop.features.budget.shared.repository;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.budget.shared.mapper.BudgetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        Budget budget = BudgetMapper.toDomain(entity);

        List<BudgetItem> items = budgetItemJpaRepository.findByBudgetId(entity.getId()).stream()
                .map(BudgetMapper::itemToDomain)
                .toList();
        budget.setItems(new ArrayList<>(items));

        return budget;
    }
}
