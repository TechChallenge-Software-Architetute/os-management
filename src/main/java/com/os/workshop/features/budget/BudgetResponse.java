package com.os.workshop.features.budget;

import com.os.workshop.features.budget.domain.Budget;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Budget generated for a service order.")
public record BudgetResponse(
        @Schema(description = "Budget unique identifier.", example = "1")
        Long id,

        @Schema(description = "Service order identifier associated with this budget.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601")
        UUID serviceOrderId,

        @Schema(description = "Total budget price.", example = "249.90")
        BigDecimal totalPrice,

        @Schema(description = "Budget items generated from reserved stock.")
        List<BudgetItemResponse> items,

        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
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
