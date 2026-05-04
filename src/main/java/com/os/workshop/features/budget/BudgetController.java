package com.os.workshop.features.budget;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
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
@Tag(name = "Budgets", description = "Read budgets generated from service order stock reservations.")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    /**
     * Retrieves the budget for a service order.
     * The budget is automatically generated from active stock reservations.
     * If no reservations exist yet, returns 404.
     *
     * @param serviceOrderId the UUID of the service order
     * @return the budget with all items and total price
     */
    @Operation(summary = "Find budget by service order", description = "Find budget by service order endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<FindBudgetByServiceOrderResponse> findByServiceOrder(@PathVariable UUID serviceOrderId) {
        return findBudgetByServiceOrderHandler.handle(serviceOrderId)
                .map(budget -> ResponseEntity.ok(FindBudgetByServiceOrderResponse.from(budget)))
                .orElse(ResponseEntity.notFound().build());
    }
}
