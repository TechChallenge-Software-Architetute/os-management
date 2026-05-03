package com.os.workshop.features.budget.repository;

import com.os.workshop.features.budget.domain.Budget;

import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {

    Budget save(Budget budget);

    Optional<Budget> findByServiceOrderId(UUID serviceOrderId);

    void deleteItemsByBudgetId(Long budgetId);
}
