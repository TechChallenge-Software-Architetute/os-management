package com.os.workshop.adapter.in.web.budget;

import com.os.workshop.application.budget.FindBudgetByServiceOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final FindBudgetByServiceOrderUseCase findBudgetByServiceOrderUseCase;

    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<BudgetResponse> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return findBudgetByServiceOrderUseCase.execute(serviceOrderId)
                .map(budget -> ResponseEntity.ok(BudgetResponse.from(budget)))
                .orElse(ResponseEntity.notFound().build());
    }
}
