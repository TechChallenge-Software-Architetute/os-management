package com.os.workshop.adapter.in.web.client;

import com.os.workshop.domain.budget.Budget;
import com.os.workshop.domain.budget.BudgetItem;
import com.os.workshop.domain.serviceorder.ServiceOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FindMyOrderDetailResponse(
        UUID orderId, String placaVeiculo, String serviceStatus,
        List<String> services, BudgetSummary budget
) {
    public static FindMyOrderDetailResponse from(ServiceOrder order, Budget budget) {
        BudgetSummary budgetSummary = null;
        if (budget != null) {
            budgetSummary = new BudgetSummary(budget.getTotalPrice(),
                    budget.getItems().stream()
                            .map(item -> new BudgetItemSummary(item.getProductName(),
                                    item.getProductType().name(), item.getQuantity(),
                                    item.getUnitPrice(), item.getTotalPrice()))
                            .toList());
        }
        return new FindMyOrderDetailResponse(order.getId(), order.getPlacaVeiculo(),
                order.getServiceStatus(), order.getListService(), budgetSummary);
    }

    public record BudgetSummary(BigDecimal totalPrice, List<BudgetItemSummary> items) {}
    public record BudgetItemSummary(String productName, String productType,
                                     BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice) {}
}
