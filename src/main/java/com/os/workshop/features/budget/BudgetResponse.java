package com.os.workshop.features.budget;

import com.os.workshop.features.budget.domain.Budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(
        Long id,
        UUID serviceOrderId,
        BigDecimal totalPrice,
        List<BudgetItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getServiceOrderId(),
                budget.getTotalPrice(),
                budget.getItems().stream().map(BudgetItemResponse::from).toList(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
