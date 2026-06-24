package com.os.workshop.features.client.myOrderDetail;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Detailed view of a service order for the client portal.
 * Includes the budget with item-level price breakdown when available.
 */
@Schema(description = "Find My Order Detail response payload.")
public record FindMyOrderDetailResponse(
        @Schema(description = "Order identifier.", example = "11111111-1111-1111-1111-111111111111") UUID orderId,
        @Schema(description = "Placa Veiculo.", example = "ABC-1234") String placaVeiculo,
        @Schema(description = "Service Status.", example = "RECEBIDA") String serviceStatus,
        @Schema(description = "Services.", example = "example") List<String> services,
        @Schema(description = "Budget.", example = "{}") BudgetSummary budget
) {
    public static FindMyOrderDetailResponse from(ServiceOrderEntity entity, FindBudgetByServiceOrderResponse budget) {
        BudgetSummary budgetSummary = null;
        if (budget != null) {
            budgetSummary = new BudgetSummary(
                    budget.totalPrice(),
                    budget.items().stream()
                            .map(item -> new BudgetItemSummary(
                                    item.productName(),
                                    item.productType().name(),
                                    item.quantity(),
                                    item.unitPrice(),
                                    item.totalPrice()
                            ))
                            .toList()
            );
        }

        return new FindMyOrderDetailResponse(
                entity.getId(),
                entity.getPlacaVeiculo(),
                entity.getServiceStatus(),
                entity.getListService(),
                budgetSummary
        );
    }

    public record BudgetSummary(
            BigDecimal totalPrice,
            List<BudgetItemSummary> items
    ) {}

    public record BudgetItemSummary(
            String productName,
            String productType,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
}
