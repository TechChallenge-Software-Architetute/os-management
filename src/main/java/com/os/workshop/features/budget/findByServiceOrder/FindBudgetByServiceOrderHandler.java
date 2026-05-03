package com.os.workshop.features.budget.findByServiceOrder;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles finding a budget by service order ID.
 */
@Service
@RequiredArgsConstructor
public class FindBudgetByServiceOrderHandler {

    private final BudgetRepository budgetRepository;

    /**
     * Retrieves the budget for a service order, if it exists.
     *
     * @param serviceOrderId the UUID of the service order
     * @return the budget, or empty if no budget exists yet
     */
    @Transactional(readOnly = true)
    public Optional<Budget> handle(UUID serviceOrderId) {
        return budgetRepository.findByServiceOrderId(serviceOrderId);
    }
}
