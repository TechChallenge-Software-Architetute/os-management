package com.os.workshop.adapter.in.web.budget;

import com.os.workshop.domain.budget.Budget;
import com.os.workshop.domain.budget.BudgetItem;
import com.os.workshop.domain.product.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(
        Long id, UUID serviceOrderId, BigDecimal totalPrice,
        List<BudgetItemResponse> items, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(budget.getId(), budget.getServiceOrderId(), budget.getTotalPrice(),
                budget.getItems().stream().map(BudgetItemResponse::from).toList(),
                budget.getCreatedAt(), budget.getUpdatedAt());
    }

    public record BudgetItemResponse(
            Long id, Long productId, String productName, String productSku,
            ProductType productType, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice
    ) {
        public static BudgetItemResponse from(BudgetItem item) {
            return new BudgetItemResponse(item.getId(), item.getProductId(), item.getProductName(),
                    item.getProductSku(), item.getProductType(), item.getQuantity(),
                    item.getUnitPrice(), item.getTotalPrice());
        }
    }
}
