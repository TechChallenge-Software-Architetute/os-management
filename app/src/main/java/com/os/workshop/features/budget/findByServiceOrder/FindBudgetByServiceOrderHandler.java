package com.os.workshop.features.budget.findByServiceOrder;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Retrieves the budget for a service order, if it exists.
 */
@Service
@RequiredArgsConstructor
public class FindBudgetByServiceOrderHandler {

    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public Optional<Budget> handle(UUID serviceOrderId) {
        return budgetRepository.findByServiceOrderId(serviceOrderId);
    }
}
