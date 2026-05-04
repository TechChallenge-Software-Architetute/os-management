package com.os.workshop.features.budget.findByServiceOrder;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.product.shared.domain.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Find Budget By Service Order response payload.")
public record FindBudgetByServiceOrderResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Service Order identifier.", example = "11111111-1111-1111-1111-111111111111") UUID serviceOrderId,
        @Schema(description = "Total Price.", example = "179.80") BigDecimal totalPrice,
        @Schema(description = "Items.", example = "[]") List<BudgetItemResponse> items,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
) {
    public static FindBudgetByServiceOrderResponse from(Budget budget) {
        return new FindBudgetByServiceOrderResponse(
                budget.getId(),
                budget.getServiceOrderId(),
                budget.getTotalPrice(),
                budget.getItems().stream().map(BudgetItemResponse::from).toList(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    public record BudgetItemResponse(
            Long id,
            Long productId,
            String productName,
            String productSku,
            ProductType productType,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        public static BudgetItemResponse from(BudgetItem item) {
            return new BudgetItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductSku(),
                    item.getProductType(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            );
        }
    }
}
