package com.os.workshop.features.budget.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetItemJpaRepository extends JpaRepository<BudgetItemEntity, Long> {

    List<BudgetItemEntity> findByBudgetId(Long budgetId);

    void deleteByBudgetId(Long budgetId);
}
