package com.os.workshop.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for reading budgets.
 * Budgets are automatically calculated when stock reservations change.
 * This controller only exposes read operations.
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Retrieves the budget for a service order.
     * The budget is automatically generated from active stock reservations.
     * If no reservations exist yet, returns 404.
     *
     * @param serviceOrderId the UUID of the service order
     * @return the budget with all items and total price
     */
    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<BudgetResponse> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return budgetService.findByServiceOrderId(serviceOrderId)
                .map(budget -> ResponseEntity.ok(BudgetResponse.from(budget)))
                .orElse(ResponseEntity.notFound().build());
    }
}
