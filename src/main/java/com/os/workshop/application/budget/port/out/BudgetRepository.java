package com.os.workshop.application.budget.port.out;

import com.os.workshop.domain.budget.Budget;

import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findByServiceOrderId(UUID serviceOrderId);
    void deleteItemsByBudgetId(Long budgetId);
}
