package com.os.workshop.features.budget;

import com.os.workshop.features.common.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Budgets", description = "Read budgets generated from stock reservations for service orders.")
public class BudgetController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);

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
    @Operation(summary = "Find budget by service order", description = "Retrieves the budget automatically generated for a service order from active stock reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget found", content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found for the service order", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> findByServiceOrder(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID serviceOrderId) {
        logger.info("Finding budget by service order. serviceOrderId={}", serviceOrderId);
        return budgetService.findByServiceOrderId(serviceOrderId)
                .map(budget -> ResponseEntity.ok(BudgetResponse.from(budget)))
                .orElse(ResponseEntity.notFound().build());
    }
}
